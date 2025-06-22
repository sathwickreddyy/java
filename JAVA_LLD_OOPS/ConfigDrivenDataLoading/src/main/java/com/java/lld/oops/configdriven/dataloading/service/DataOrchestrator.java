package com.java.lld.oops.configdriven.dataloading.service;

import com.java.lld.oops.configdriven.dataloading.config.DataLoaderConfiguration;
import com.java.lld.oops.configdriven.dataloading.factory.DataSourcingFactory;
import com.java.lld.oops.configdriven.dataloading.model.DataRecord;
import com.java.lld.oops.configdriven.dataloading.model.ExecutionResult;
import com.java.lld.oops.configdriven.dataloading.model.LoadingStats;
import com.java.lld.oops.configdriven.dataloading.utils.DataRecordPrinter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Orchestrates the full lifecycle of data loading from sourcing,
 * processing, to database writing for a given data source configuration.
 *
 * Supports synchronous and asynchronous execution modes,
 * and captures audit metrics for each run.
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
     * Executes the data loading pipeline for a specific data source.
     * Handles loading, processing, and optional database writing.
     *
     * @param dataSourceName name of the configured data source
     * @return {@link ExecutionResult} summarizing the outcome
     */
    public ExecutionResult executeDataSource(String dataSourceName) {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("Initiating execution for data source: '{}'", dataSourceName);

        try {
            DataLoaderConfiguration.DataSourceDefinition config =
                    Optional.ofNullable(dataLoaderConfiguration.dataSources().get(dataSourceName))
                            .orElseThrow(() -> new IllegalArgumentException("Data source not found: " + dataSourceName));

            log.debug("Configuration resolved for '{}': {}", dataSourceName, config);

            // Load data
            var loader = dataSourceFactory.getLoader(config.type());
            var dataStream = loader.loadData(config);
            log.info("Data loaded for '{}'. Proceeding to processing.", dataSourceName);

            // Process data
            var processedStream = dataProcessor.processData(dataStream, config);
            log.info("Data processing complete for '{}'", dataSourceName);

            // Write to DB if target table is configured
            if (config.target() != null && config.target().table() != null && !config.target().table().isBlank()) {
                LoadingStats stats = databaseWriter.writeData(processedStream, config);
                LocalDateTime endTime = LocalDateTime.now();

                log.info("Completed data load for '{}'. Records/sec: {}, Duration: {}ms",
                        dataSourceName, stats.recordsPerSecond(), stats.writeTimeMs());

                return ExecutionResult.success(
                        dataSourceName,
                        (int) stats.recordsPerSecond(),
                        (int) stats.recordsPerSecond(),
                        startTime,
                        endTime,
                        stats
                );
            }

            // No DB write: for testing or dry-run
            List<DataRecord> records = processedStream.toList();
            log.warn("Target table not configured for '{}'. Skipping DB write. Record count: {}", dataSourceName, records.size());

            // @TODO: remove this post-testing
            DataRecordPrinter.printRecords(records);

            LocalDateTime endTime = LocalDateTime.now();
            return ExecutionResult.success(
                    dataSourceName,
                    records.size(),
                    records.size(),
                    startTime,
                    endTime,
                    null
            );

        } catch (IllegalArgumentException e) {
            log.error("Configuration error for data source '{}': {}", dataSourceName, e.getMessage());
            return ExecutionResult.failure(dataSourceName, List.of(e.getMessage()), startTime, LocalDateTime.now());

        } catch (Exception e) {
            log.error("Unexpected failure during execution of data source '{}'", dataSourceName, e);
            return ExecutionResult.failure(dataSourceName, List.of("Internal error: " + e.getMessage()), startTime, LocalDateTime.now());
        }
    }

    /**
     * Executes the data loading pipeline asynchronously for the given data source.
     *
     * @param dataSourceName name of the configured data source
     * @return {@link CompletableFuture} containing the {@link ExecutionResult}
     */
    public CompletableFuture<ExecutionResult> executeDataSourceAsync(String dataSourceName) {
        return CompletableFuture.completedFuture(executeDataSource(dataSourceName));
    }

    /**
     * Executes all configured data sources sequentially.
     *
     * @return list of {@link ExecutionResult} for each data source
     */
    public List<ExecutionResult> executeAllDataSources() {
        log.info("Executing all configured data sources...");
        return dataLoaderConfiguration.dataSources().keySet().stream()
                .map(this::executeDataSource)
                .toList();
    }
}