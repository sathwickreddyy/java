package com.java.lld.oops.configdriven.dataloading.service;

import com.java.lld.oops.configdriven.dataloading.config.DataLoaderConfiguration;
import com.java.lld.oops.configdriven.dataloading.factory.DataSourcingFactory;
import com.java.lld.oops.configdriven.dataloading.model.DataRecord;
import com.java.lld.oops.configdriven.dataloading.model.ExecutionResult;
import com.java.lld.oops.configdriven.dataloading.utils.DataRecordPrinter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class DataOrchestrator {

    private final DataSourcingFactory dataSourceFactory;
    private final DataProcessor dataProcessor;
//    private final DatabaseWriter databaseWriter;
    private final DataLoaderConfiguration dataLoaderConfiguration;

    public DataOrchestrator(DataSourcingFactory dataSourceFactory,
                            DataProcessor dataProcessor,
//                            DatabaseWriter databaseWriter,
                            DataLoaderConfiguration dataLoaderConfiguration) {
        this.dataSourceFactory = dataSourceFactory;
        this.dataProcessor = dataProcessor;
//        this.databaseWriter = databaseWriter;
        this.dataLoaderConfiguration = dataLoaderConfiguration;
    }

    public ExecutionResult executeDataSource(String dataSourceName) {
        LocalDateTime startTime = LocalDateTime.now();

        try {
            log.info("Starting execution for data source: {}", dataSourceName);

            DataLoaderConfiguration.DataSourceDefinition config = dataLoaderConfiguration.dataSources().get(dataSourceName);
            if (config == null) {
                throw new IllegalArgumentException("Data source not found: " + dataSourceName);
            }

            // Load data
            var loader = dataSourceFactory.getLoader(config.type());
            var dataStream = loader.loadData(config);

            // Process data
            var processedStream = dataProcessor.processData(dataStream, config);

//            // Write to database
//            LoadingStats stats = databaseWriter.writeData(processedStream, config);
            // Print Records
            List<DataRecord> records = processedStream.toList();
            DataRecordPrinter.printRecords(records);

            LocalDateTime endTime = LocalDateTime.now();

//            log.info("Completed execution for data source: {} in {} ms",
//                    dataSourceName, stats.writeTimeMs());

//            return ExecutionResult.success(dataSourceName, (int) stats.recordsPerSecond(),
//                    (int) stats.recordsPerSecond(), startTime, endTime, stats);
            return ExecutionResult.success(dataSourceName, records.size(), records.size(), startTime, endTime, null);

        } catch (Exception e) {
            LocalDateTime endTime = LocalDateTime.now();
            log.error("Error executing data source: {}", dataSourceName, e);

            return ExecutionResult.failure(dataSourceName, List.of(e.getMessage()),
                    startTime, endTime);
        }
    }

//    @Async
    public CompletableFuture<ExecutionResult> executeDataSourceAsync(String dataSourceName) {
        return CompletableFuture.completedFuture(executeDataSource(dataSourceName));
    }

    public List<ExecutionResult> executeAllDataSources() {
        return dataLoaderConfiguration.dataSources().keySet().stream()
                .map(this::executeDataSource)
                .toList();
    }
}