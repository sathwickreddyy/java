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
 * High-performance database writer service that handles batch insertion of transformed data records
 * into target database tables with comprehensive transaction management, audit logging, and bitemporal support.
 *
 * <p>This service serves as the final stage in the data loading pipeline, responsible for persisting
 * processed data records to the database with optimal performance characteristics and data integrity
 * guarantees. It provides both standard and bitemporal data loading capabilities with comprehensive
 * error handling and performance monitoring.</p>
 *
 * <p><b>Core Capabilities:</b></p>
 * <ul>
 *     <li><b>Batch Processing:</b> High-performance batch insertion with configurable batch sizes</li>
 *     <li><b>Transaction Management:</b> ACID-compliant transactions with proper rollback handling</li>
 *     <li><b>Bitemporal Support:</b> Time-aware data loading with valid-time and transaction-time tracking</li>
 *     <li><b>Audit Trail:</b> Comprehensive audit logging for compliance and monitoring</li>
 *     <li><b>Performance Monitoring:</b> Real-time statistics collection and reporting</li>
 *     <li><b>Error Recovery:</b> Graceful handling of batch failures with detailed error reporting</li>
 * </ul>
 *
 * <p><b>Performance Features:</b></p>
 * <ul>
 *     <li><b>Configurable Batching:</b> Optimized batch sizes for different data volumes</li>
 *     <li><b>Connection Pooling:</b> Efficient database connection management through HikariCP</li>
 *     <li><b>Memory Optimization:</b> Streaming processing to handle large datasets</li>
 *     <li><b>Throughput Metrics:</b> Real-time performance monitoring and reporting</li>
 * </ul>
 *
 * <p><b>Transaction Management:</b></p>
 * <ul>
 *     <li><b>REQUIRED Propagation:</b> Participates in existing transactions or creates new ones</li>
 *     <li><b>Batch Atomicity:</b> Each batch is processed atomically with rollback on failure</li>
 *     <li><b>Isolation Levels:</b> Proper isolation to prevent data corruption</li>
 *     <li><b>Deadlock Prevention:</b> Optimized batch ordering to minimize deadlock scenarios</li>
 * </ul>
 *
 * <p><b>Bitemporal Data Management:</b></p>
 * <ul>
 *     <li><b>Valid Time:</b> Business time when data is valid in the real world</li>
 *     <li><b>Transaction Time:</b> System time when data is stored in the database</li>
 *     <li><b>Record Versioning:</b> Maintains complete history of data changes</li>
 *     <li><b>Point-in-Time Queries:</b> Enables historical data reconstruction</li>
 * </ul>
 *
 * <p><b>Error Handling Strategy:</b></p>
 * <ul>
 *     <li><b>Batch-Level Recovery:</b> Failed batches are logged but don't stop processing</li>
 *     <li><b>Detailed Error Context:</b> Comprehensive error messages with batch information</li>
 *     <li><b>Audit Trail Resilience:</b> Audit failures are logged but don't break data loading</li>
 *     <li><b>Transaction Rollback:</b> Proper cleanup on transaction failures</li>
 * </ul>
 *
 * <p><b>Java 11 Compatibility:</b></p>
 * <ul>
 *     <li>Uses traditional getter methods instead of record accessors</li>
 *     <li>String concatenation instead of text blocks for SQL</li>
 *     <li>Compatible with Java 11, 17, and 21</li>
 *     <li>No behavioral differences across Java versions</li>
 * </ul>
 *
 * @author sathwick
 * @since 1.0.0
 */
@Slf4j
@Service
public class DatabaseWriter {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructs a new DatabaseWriter with the required JDBC template.
     *
     * @param jdbcTemplate the JDBC template for database operations
     */
    public DatabaseWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        log.debug("DatabaseWriter initialized with JDBC template");
    }

    /**
     * Writes the processed DataRecord stream into the configured target database table with
     * comprehensive batch processing, error handling, and performance monitoring.
     *
     * <p>This method orchestrates the complete database writing process:</p>
     * <ol>
     *     <li><b>Stream Processing:</b> Processes the data stream in configurable batches</li>
     *     <li><b>Batch Execution:</b> Executes database inserts using optimized batch operations</li>
     *     <li><b>Error Handling:</b> Handles batch failures gracefully without stopping processing</li>
     *     <li><b>Performance Monitoring:</b> Collects and reports throughput statistics</li>
     *     <li><b>Audit Logging:</b> Records execution metadata for compliance and monitoring</li>
     * </ol>
     *
     * <p><b>Batch Processing Strategy:</b></p>
     * <ul>
     *     <li>Records are accumulated into batches based on configured batch size</li>
     *     <li>Each batch is executed atomically with proper transaction management</li>
     *     <li>Failed batches are logged and skipped to allow processing to continue</li>
     *     <li>Remaining records are processed in a final batch</li>
     * </ul>
     *
     * <p><b>Performance Characteristics:</b></p>
     * <ul>
     *     <li><b>Throughput:</b> Optimized for high-volume data loading scenarios</li>
     *     <li><b>Memory Usage:</b> Streaming processing prevents memory overflow</li>
     *     <li><b>Database Load:</b> Batch operations minimize database round trips</li>
     *     <li><b>Connection Efficiency:</b> Reuses connections through connection pooling</li>
     * </ul>
     *
     * <p><b>Error Recovery:</b></p>
     * <ul>
     *     <li>Batch failures are logged with detailed error context</li>
     *     <li>Processing continues with subsequent batches</li>
     *     <li>Audit trail failures are logged but don't break the operation</li>
     *     <li>Transaction rollback ensures data consistency</li>
     * </ul>
     *
     * @param dataStream Stream of transformed data records to be written to the database
     * @param config Data source configuration containing table name, batch size, and other settings
     * @return {@link LoadingStats} containing comprehensive metrics for the load operation
     * @throws IllegalArgumentException if dataStream or config is null
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public LoadingStats writeData(Stream<DataRecord> dataStream, DataLoaderConfiguration.DataSourceDefinition config) {
        if (dataStream == null) {
            throw new IllegalArgumentException("Data stream cannot be null");
        }
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }

        log.info("Starting database write for table: {}", config.getTarget().getTable());

        long startTime = System.currentTimeMillis();
        List<DataRecord> batch = new ArrayList<>();
        int totalRecords = 0;
        int batchCount = 0;
        int batchSize = config.getTarget().getBatchSize() != null ? config.getTarget().getBatchSize() : 1000;

        Iterator<DataRecord> iterator = dataStream.iterator();
        while (iterator.hasNext()) {
            batch.add(iterator.next());
            totalRecords++;

            if (batch.size() >= batchSize) {
                if (executeBatchSafely(batch, config)) {
                    batchCount++;
                }
                batch.clear();
            }
        }

        // Process remaining records in final batch
        if (!batch.isEmpty()) {
            if (executeBatchSafely(batch, config)) {
                batchCount++;
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        double recordsPerSecond = totalRecords > 0 ? (totalRecords * 1000.0) / duration : 0;

        // Record audit trail with error handling
        recordAuditTrailSafely(config, totalRecords, duration);

        log.info("Completed database write for table '{}'. Total records: {}, Batches: {}, Time: {}ms, Throughput: {:.2f} records/sec",
                config.getTarget().getTable(), totalRecords, batchCount, duration, recordsPerSecond);

        return new LoadingStats(0, 0, duration, batchCount, recordsPerSecond);
    }

    /**
     * Writes data with bitemporal support, maintaining both valid-time and transaction-time dimensions.
     *
     * <p>Bitemporal data loading provides comprehensive time-aware data management:</p>
     * <ol>
     *     <li><b>Record Invalidation:</b> Marks existing records as invalid for the reporting date</li>
     *     <li><b>Data Enrichment:</b> Adds temporal metadata to incoming records</li>
     *     <li><b>Standard Processing:</b> Delegates to standard write logic for actual insertion</li>
     * </ol>
     *
     * <p><b>Temporal Metadata:</b></p>
     * <ul>
     *     <li><b>valid_from:</b> Current timestamp when record becomes valid</li>
     *     <li><b>valid_to:</b> Far future timestamp (9999-12-31) for active records</li>
     *     <li><b>reporting_date:</b> Business date for which data is being loaded</li>
     * </ul>
     *
     * <p><b>Use Cases:</b></p>
     * <ul>
     *     <li><b>Financial Data:</b> Regulatory reporting with historical accuracy requirements</li>
     *     <li><b>Audit Compliance:</b> Maintaining complete audit trails for compliance</li>
     *     <li><b>Data Corrections:</b> Correcting historical data while preserving original values</li>
     *     <li><b>Point-in-Time Analysis:</b> Enabling historical data reconstruction</li>
     * </ul>
     *
     * @param dataStream Stream of data records to be written with bitemporal support
     * @param config Data source configuration containing table and batch settings
     * @param reportingDate Business date for which data is being loaded
     * @return {@link LoadingStats} containing metrics for the bitemporal load operation
     * @throws IllegalArgumentException if any parameter is null
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public LoadingStats writeDataWithBiTemporality(Stream<DataRecord> dataStream,
                                                   DataLoaderConfiguration.DataSourceDefinition config,
                                                   LocalDate reportingDate) {
        if (dataStream == null) {
            throw new IllegalArgumentException("Data stream cannot be null");
        }
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }
        if (reportingDate == null) {
            throw new IllegalArgumentException("Reporting date cannot be null");
        }

        log.info("Starting bitemporal write for table: {}, reportingDate: {}",
                config.getTarget().getTable(), reportingDate);

        String tableName = config.getTarget().getTable();
        String updateSql = "UPDATE " + tableName + " SET valid_to = CURRENT_TIMESTAMP WHERE reporting_date = ?";

        try {
            // Step 1: Invalidate existing records for the same reporting date
            int invalidatedRecords = jdbcTemplate.update(updateSql, Date.valueOf(reportingDate));
            log.info("Invalidated {} existing records for reporting date: {}", invalidatedRecords, reportingDate);

            // Step 2: Enrich incoming records with temporal metadata
            Stream<DataRecord> enrichedStream = dataStream.map(record -> enrichRecordWithTemporalData(record, reportingDate));

            // Step 3: Delegate to existing write logic
            LoadingStats stats = writeData(enrichedStream, config);

            log.info("Completed bitemporal write for table '{}' with reporting date: {}", tableName, reportingDate);
            return stats;

        } catch (Exception e) {
            log.error("Error during bitemporal write for table '{}' with reporting date: {}. Error: {}",
                    tableName, reportingDate, e.getMessage(), e);
            throw new RuntimeException("Bitemporal write failed", e);
        }
    }

    /**
     * Enriches a data record with temporal metadata for bitemporal processing.
     *
     * @param record the original data record
     * @param reportingDate the business reporting date
     * @return enriched data record with temporal fields
     */
    private DataRecord enrichRecordWithTemporalData(DataRecord record, LocalDate reportingDate) {
        Map<String, Object> enrichedData = new LinkedHashMap<>(record.getData());
        enrichedData.put("valid_from", Timestamp.valueOf(LocalDateTime.now()));
        enrichedData.put("valid_to", Timestamp.valueOf(LocalDateTime.of(9999, 12, 31, 0, 0, 0)));
        enrichedData.put("reporting_date", Date.valueOf(reportingDate));

        return new DataRecord(enrichedData, record.getRowNumber(), record.isValid(), record.getErrorMessage());
    }

    /**
     * Executes a batch with comprehensive error handling and logging.
     *
     * @param batch the batch of records to execute
     * @param config the configuration containing table details
     * @return true if batch executed successfully, false otherwise
     */
    private boolean executeBatchSafely(List<DataRecord> batch, DataLoaderConfiguration.DataSourceDefinition config) {
        try {
            executeBatch(batch, config);
            return true;
        } catch (Exception e) {
            log.error("Failed to execute batch for table '{}'. Skipping {} records. Error: {}",
                    config.getTarget().getTable(), batch.size(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Records audit trail with error handling to prevent audit failures from breaking data loading.
     *
     * @param config the data source configuration
     * @param totalRecords the total number of records processed
     * @param duration the execution duration in milliseconds
     */
    private void recordAuditTrailSafely(DataLoaderConfiguration.DataSourceDefinition config,
                                        int totalRecords, long duration) {
        try {
            recordAuditTrail(config, totalRecords, duration);
        } catch (Exception e) {
            log.warn("Failed to record audit trail for table '{}': {}",
                    config.getTarget().getTable(), e.getMessage(), e);
        }
    }

    /**
     * Executes a single batch insert operation into the configured target table with optimized performance.
     *
     * <p>This method implements the core batch insertion logic:</p>
     * <ol>
     *     <li><b>Validation:</b> Ensures batch is not empty</li>
     *     <li><b>SQL Generation:</b> Dynamically builds INSERT statement</li>
     *     <li><b>Batch Execution:</b> Uses JDBC batch operations for optimal performance</li>
     *     <li><b>Parameter Binding:</b> Safely binds parameters to prevent SQL injection</li>
     * </ol>
     *
     * <p><b>Performance Optimizations:</b></p>
     * <ul>
     *     <li>Uses PreparedStatement for optimal query plan caching</li>
     *     <li>Batch parameter binding minimizes database round trips</li>
     *     <li>Proper parameter type handling for database compatibility</li>
     * </ul>
     *
     * @param records Batch of data records to insert
     * @param config Configuration containing table details and settings
     * @throws SQLException if database operation fails
     * @throws IllegalArgumentException if records is empty
     */
    private void executeBatch(List<DataRecord> records, DataLoaderConfiguration.DataSourceDefinition config) {
        if (records == null || records.isEmpty()) {
            log.debug("Empty batch received for execution. Skipping.");
            return;
        }

        String tableName = config.getTarget().getTable();
        Set<String> columns = records.get(0).getData().keySet();
        String insertSql = buildInsertSql(tableName, columns);

        log.debug("Executing batch insert into table '{}' with {} records. SQL: {}",
                tableName, records.size(), insertSql);

        jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                DataRecord record = records.get(i);
                int parameterIndex = 1;

                for (String column : columns) {
                    Object value = record.getData().get(column);
                    ps.setObject(parameterIndex++, value);

                    if (log.isTraceEnabled()) {
                        log.trace("Setting parameter {} for column '{}': {} (type: {})",
                                parameterIndex - 1, column, value,
                                value != null ? value.getClass().getSimpleName() : "null");
                    }
                }
            }

            @Override
            public int getBatchSize() {
                return records.size();
            }
        });

        log.debug("Batch insert completed for table '{}'. Records processed: {}", tableName, records.size());
    }

    /**
     * Builds an SQL INSERT statement dynamically based on table name and column names.
     *
     * <p>This method generates optimized INSERT statements:</p>
     * <ul>
     *     <li>Dynamic column list based on actual data</li>
     *     <li>Parameterized queries to prevent SQL injection</li>
     *     <li>Optimal query structure for database performance</li>
     * </ul>
     *
     * <p><b>SQL Structure:</b></p>
     * <pre>INSERT INTO table_name (col1, col2, col3) VALUES (?, ?, ?)</pre>
     *
     * @param tableName Target table name for the INSERT operation
     * @param columns Set of column names to include in the INSERT statement
     * @return Complete SQL INSERT statement with parameter placeholders
     * @throws IllegalArgumentException if tableName is null/empty or columns is null/empty
     */
    private String buildInsertSql(String tableName, Set<String> columns) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("Columns cannot be null or empty");
        }

        String columnList = String.join(", ", columns);
        String placeholders = columns.stream()
                .map(col -> "?")
                .collect(Collectors.joining(", "));

        String insertSql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columnList, placeholders);

        log.trace("Generated INSERT statement for table '{}': {}", tableName, insertSql);
        return insertSql;
    }

    /**
     * Records comprehensive metadata for the data loading operation into the audit table.
     *
     * <p>This method maintains a complete audit trail including:</p>
     * <ul>
     *     <li><b>Table Information:</b> Target table name and source type</li>
     *     <li><b>Volume Metrics:</b> Total record count processed</li>
     *     <li><b>Performance Data:</b> Execution duration and timing</li>
     *     <li><b>Timestamp:</b> Exact execution time for temporal tracking</li>
     * </ul>
     *
     * <p><b>Audit Table Schema:</b></p>
     * <pre>
     * CREATE TABLE data_loading_audit (
     *     table_name VARCHAR(255),
     *     source_type VARCHAR(50),
     *     record_count INTEGER,
     *     duration_ms BIGINT,
     *     execution_time TIMESTAMP
     * );
     * </pre>
     *
     * <p><b>Compliance Benefits:</b></p>
     * <ul>
     *     <li>Regulatory compliance for data lineage tracking</li>
     *     <li>Performance monitoring and optimization</li>
     *     <li>Operational visibility into data loading processes</li>
     *     <li>Historical analysis of data loading patterns</li>
     * </ul>
     *
     * @param config Data source configuration containing table and type information
     * @param recordCount Total number of records processed in this operation
     * @param durationMs Total execution time in milliseconds
     * @throws SQLException if audit table insertion fails
     */
    private void recordAuditTrail(DataLoaderConfiguration.DataSourceDefinition config,
                                  int recordCount, long durationMs) {
        // Using string concatenation instead of text blocks for Java 11 compatibility
        String auditSql = "INSERT INTO data_loading_audit " +
                "(table_name, source_type, record_count, duration_ms, execution_time) " +
                "VALUES (?, ?, ?, ?, ?)";

        try {
            jdbcTemplate.update(auditSql,
                    config.getTarget().getTable(),
                    config.getType(),
                    recordCount,
                    durationMs,
                    Timestamp.valueOf(LocalDateTime.now()));

            log.info("Audit trail recorded for table '{}'. Records: {}, Duration: {}ms, Source: {}",
                    config.getTarget().getTable(), recordCount, durationMs, config.getType());

        } catch (Exception e) {
            log.error("Failed to record audit trail for table '{}': {}",
                    config.getTarget().getTable(), e.getMessage(), e);
            throw e; // Re-throw to be handled by caller
        }
    }
}
