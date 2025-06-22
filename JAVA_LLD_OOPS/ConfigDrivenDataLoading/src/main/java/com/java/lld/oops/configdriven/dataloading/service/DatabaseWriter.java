package com.java.lld.oops.configdriven.dataloading.service;

import com.java.lld.oops.configdriven.dataloading.config.DataLoaderConfiguration;
import com.java.lld.oops.configdriven.dataloading.model.DataRecord;
import com.java.lld.oops.configdriven.dataloading.model.LoadingStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service responsible for writing transformed data records into the database
 * using JDBC batch operations. It also maintains an audit trail of the load.
 * Supports transactional writes with batching, and collects loading statistics.
 *
 * @author sathwick
 */
@Slf4j
@Service
public class DatabaseWriter {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Writes the processed {@link DataRecord} stream into the configured target database table.
     * Handles batching and audit logging.
     *
     * @param dataStream Stream of transformed data records
     * @param config     Data source configuration containing table and batch settings
     * @return {@link LoadingStats} containing metrics for the load operation
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public LoadingStats writeData(Stream<DataRecord> dataStream, DataLoaderConfiguration.DataSourceDefinition config) {
        log.info("Starting database write for table: {}", config.target().table());

        long startTime = System.currentTimeMillis();

        List<DataRecord> batch = new ArrayList<>();
        int totalRecords = 0;
        int batchCount = 0;

        var iterator = dataStream.iterator();
        while (iterator.hasNext()) {
            batch.add(iterator.next());
            totalRecords++;

            if (config.target().batchSize() != null && batch.size() >= config.target().batchSize()) {
                try {
                    executeBatch(batch, config);
                    batchCount++;
                } catch (Exception e) {
                    log.error("Failed to execute batch for table '{}'. Skipping {} records. Error: {}",
                            config.target().table(), batch.size(), e.getMessage(), e);
                }
                batch.clear();
            }
        }

        // Flush remaining records
        if (!batch.isEmpty()) {
            try {
                executeBatch(batch, config);
                batchCount++;
            } catch (Exception e) {
                log.error("Failed to execute final batch for table '{}'. Skipping {} records. Error: {}",
                        config.target().table(), batch.size(), e.getMessage(), e);
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        double recordsPerSecond = totalRecords > 0 ? (totalRecords * 1000.0) / duration : 0;

        // @TODO arguable to move this to a separate service / remove from here
        try {
            recordAuditTrail(config, totalRecords, duration);
        } catch (Exception e) {
            log.warn("Failed to record audit trail for table '{}': {}", config.target().table(), e.getMessage(), e);
        }

        log.info("Completed database write for table '{}'. Total records: {}, Batches: {}, Time: {}ms",
                config.target().table(), totalRecords, batchCount, duration);

        return new LoadingStats(0, 0, duration, batchCount, recordsPerSecond);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public LoadingStats writeDataWithBiTemporality(
            Stream<DataRecord> dataStream,
            DataLoaderConfiguration.DataSourceDefinition config,
            LocalDate reportingDate) {

        log.info("Starting bitemporal write for table: {}, reportingDate: {}", config.target().table(), reportingDate);

        String tableName = config.target().table();
        String updateSql = String.format(
                "UPDATE %s SET validTo = CURRENT_TIMESTAMP WHERE reportingDate = ?", tableName);

        // Step 1: Invalidate existing records for the same reportingDate
        jdbcTemplate.update(updateSql, reportingDate);

        // Step 2: Enrich incoming records with validFrom, validTo, reportingDate
        Stream<DataRecord> enrichedStream = dataStream.map(record -> {
            Map<String, Object> data = new LinkedHashMap<>(record.data());
            data.put("validFrom", Timestamp.valueOf(LocalDateTime.now()));
            data.put("validTo", Timestamp.valueOf("9999-12-31 00:00:00"));
            data.put("reportingDate", Date.valueOf(reportingDate));
            return new DataRecord(data, record.rowNumber(), record.valid(), record.errorMessage());
        });

        // Step 3: Delegate to existing write logic
        return writeData(enrichedStream, config);
    }

    /**
     * Executes a single batch insert operation into the configured target table.
     *
     * @param records Batch of data records
     * @param config  Configuration containing table details
     */
    private void executeBatch(List<DataRecord> records, DataLoaderConfiguration.DataSourceDefinition config) {
        if (records.isEmpty()) {
            log.debug("Empty batch received for execution. Skipping.");
            return;
        }

        String tableName = config.target().table();
        Set<String> columns = records.get(0).data().keySet();
        String insertSql = buildInsertSql(tableName, columns);

        log.debug("Executing batch insert into table '{}'. SQL: {}", tableName, insertSql);

        jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                DataRecord record = records.get(i);
                int index = 1;
                for (String column : columns) {
                    Object value = record.data().get(column);
                    ps.setObject(index++, value);
                    log.debug("Setting value for column '{}': {}, {}", column, value, value.getClass());
                }
            }

            @Override
            public int getBatchSize() {
                return records.size();
            }
        });

        log.debug("Batch insert completed for table '{}'. Batch size: {}", tableName, records.size());
    }

    /**
     * Builds an SQL insert statement dynamically based on column names.
     *
     * @param tableName Target table name
     * @param columns   Set of column names
     * @return SQL insert statement string
     */
    private String buildInsertSql(String tableName, Set<String> columns) {
        String columnList = String.join(", ", columns);
        String placeholders = columns.stream().map(col -> "?").collect(Collectors.joining(", "));
        log.debug("Inserting statement for table '{}'", String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columnList, placeholders));
        return String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columnList, placeholders);
    }

    /**
     * Records metadata for this data load into the audit table.
     *
     * @param config      Data source configuration
     * @param recordCount Total number of records processed
     * @param durationMs  Time taken in milliseconds
     */
    private void recordAuditTrail(DataLoaderConfiguration.DataSourceDefinition config, int recordCount, long durationMs) {
        String sql = """
            INSERT INTO data_loading_audit 
            (table_name, source_type, record_count, duration_ms, execution_time) 
            VALUES (?, ?, ?, ?, ?)
            """;

        jdbcTemplate.update(sql,
                config.target().table(),
                config.type(),
                recordCount,
                durationMs,
                Timestamp.valueOf(LocalDateTime.now()));

        log.info("Audit trail recorded for table '{}'. Total records: {}, Duration: {}ms",
                config.target().table(), recordCount, durationMs);
    }
}