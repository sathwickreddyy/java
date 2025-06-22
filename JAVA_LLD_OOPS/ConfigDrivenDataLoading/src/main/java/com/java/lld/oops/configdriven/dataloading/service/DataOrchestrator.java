package com.java.lld.oops.configdriven.dataloading.service;

import com.java.lld.oops.configdriven.dataloading.config.DataLoaderConfiguration;
import com.java.lld.oops.configdriven.dataloading.factory.DataSourcingFactory;
import com.java.lld.oops.configdriven.dataloading.model.DataRecord;
import com.java.lld.oops.configdriven.dataloading.model.ExecutionResult;
import com.java.lld.oops.configdriven.dataloading.model.LoadingStats;
import com.java.lld.oops.configdriven.dataloading.utils.DataRecordPrinter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Orchestrates the full lifecycle of data loading from sourcing,
 * processing, to database writing for a given data source configuration.
 *
 * Supports both normal and bitemporal execution flows.
 * Tracks audit metrics for each run.
 *
 * @author sathwick
 */
@Slf4j
@Service
public class DataOrchestrator {

    private final DataSourcingFactory dataSourceFactory;
    private final DataProcessor dataProcessor;
    private final DatabaseWriter databaseWriter;
    private final DataLoaderConfiguration dataLoaderConfiguration;

    public DataOrchestrator(DataSourcingFactory dataSourceFactory,
                            DataProcessor dataProcessor,
                            DatabaseWriter databaseWriter,
                            DataLoaderConfiguration dataLoaderConfiguration) {
        this.dataSourceFactory = dataSourceFactory;
        this.dataProcessor = dataProcessor;
        this.databaseWriter = databaseWriter;
        this.dataLoaderConfiguration = dataLoaderConfiguration;
    }

    /**
     * Executes the full pipeline for a given data source using normal (non-bitemporal) mode.
     *
     * @param dataSourceName the name of the configured data source
     * @return {@link ExecutionResult} summarizing the outcome
     */
    public ExecutionResult executeDataSourcing(String dataSourceName) {
        return executePipeline(dataSourceName, null);
    }

    /**
     * Executes the full pipeline in bitemporal mode for a given data source and reporting date.
     * This ensures any existing records with the same reporting date are invalidated before inserting.
     *
     * @param dataSourceName the name of the configured data source
     * @param date           the reporting date (yyyy-MM-dd)
     * @return {@link ExecutionResult} summarizing the outcome
     */
    public ExecutionResult executeDataSourcingWithBiTemporality(String dataSourceName, String date) {
        return executePipeline(dataSourceName, LocalDate.parse(date));
    }

    /**
     * Executes the full pipeline asynchronously for the given data source.
     *
     * @param dataSourceName the name of the configured data source
     * @return {@link CompletableFuture} containing the {@link ExecutionResult}
     */
    public CompletableFuture<ExecutionResult> executeDataSourceAsync(String dataSourceName) {
        return CompletableFuture.completedFuture(executeDataSourcing(dataSourceName));
    }

    /**
     * Internal pipeline executor shared between bitemporal and non-bitemporal runs.
     *
     * @param dataSourceName name of the data source
     * @param reportingDate  reporting date for bitemporal handling; null for normal mode
     * @return {@link ExecutionResult} summarizing the load outcome
     */
    private ExecutionResult executePipeline(String dataSourceName, LocalDate reportingDate) {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("Starting execution for data source '{}', mode: {}",
                dataSourceName, reportingDate != null ? "BITEMPORAL" : "NORMAL");

        try {
            DataLoaderConfiguration.DataSourceDefinition config = Optional.ofNullable(dataLoaderConfiguration.dataSources().get(dataSourceName))
                    .orElseThrow(() -> new IllegalArgumentException("Data source not found: " + dataSourceName));
            log.debug("Loaded configuration for '{}': {}", dataSourceName, config);

            // Load data from source
            var loader = dataSourceFactory.getLoader(config.type());
            var dataStream = loader.loadData(config);
            log.info("Data loaded for '{}', proceeding to processing.", dataSourceName);

            // Process data
            var processedStream = dataProcessor.processData(dataStream, config);
            log.info("Data processed for '{}'", dataSourceName);

            // TODO: remove below conversion to list after testing
            List<DataRecord> records = processedStream.toList();
            processedStream.close();
            processedStream = records.stream();

            // If table is configured, write to DB
            if (shouldWriteToDatabase(config)) {
                LoadingStats stats = (reportingDate != null)
                        ? databaseWriter.writeDataWithBiTemporality(processedStream, config, reportingDate)
                        : databaseWriter.writeData(processedStream, config);

                LocalDateTime endTime = LocalDateTime.now();
                log.info("DB write completed for '{}'. Records/sec: {}, Duration: {}ms, Batches: {}",
                        dataSourceName, stats.recordsPerSecond(), stats.writeTimeMs(), stats.batchCount());

                // @TODO remove this after testing
                // Dry-run: just print the records
                DataRecordPrinter.printRecords(records);

                return ExecutionResult.success(dataSourceName, stats.batchCount(), stats.batchCount(), startTime, endTime, stats);
            }

            // @TODO remove this after testing
            // Dry-run: just print the records
            log.warn("No target table for '{}'. Skipping DB write. Printing {} records for review.", dataSourceName, records.size());
            DataRecordPrinter.printRecords(records);

            return ExecutionResult.success(dataSourceName, records.size(), records.size(), startTime, LocalDateTime.now(), null);

        } catch (IllegalArgumentException e) {
            log.error("Configuration error for data source '{}': {}", dataSourceName, e.getMessage());
            return ExecutionResult.failure(dataSourceName, List.of(e.getMessage()), startTime, LocalDateTime.now());

        } catch (Exception e) {
            log.error("Fatal error during execution of '{}': {}", dataSourceName, e.getMessage(), e);
            return ExecutionResult.failure(dataSourceName, List.of("Internal error: " + e.getMessage()), startTime, LocalDateTime.now());
        }
    }

    /**
     * Checks if the given config has a valid DB target table configured.
     *
     * @param config the {@link DataLoaderConfiguration.DataSourceDefinition}
     * @return true if table name is present and non-blank
     */
    private boolean shouldWriteToDatabase(DataLoaderConfiguration.DataSourceDefinition config) {
        return config.target() != null &&
                config.target().table() != null &&
                !config.target().table().isBlank();
    }
}