package com.java.lld.oops.configdriven.dataloading.service;

import com.java.lld.oops.configdriven.dataloading.config.DataLoaderConfiguration;
import com.java.lld.oops.configdriven.dataloading.factory.DataSourcingFactory;
import com.java.lld.oops.configdriven.dataloading.loader.ModelConverter;
import com.java.lld.oops.configdriven.dataloading.loader.ModelLoader;
import com.java.lld.oops.configdriven.dataloading.model.DataRecord;
import com.java.lld.oops.configdriven.dataloading.model.ExecutionResult;
import com.java.lld.oops.configdriven.dataloading.model.LoadingStats;
import com.java.lld.oops.configdriven.dataloading.model.ModelLoadingResult;
import com.java.lld.oops.configdriven.dataloading.utils.DataRecordPrinter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Orchestrates the end-to-end lifecycle of data ingestion for a configured data source.
 *
 * <p>This component handles the following responsibilities:</p>
 * <ul>
 *   <li>Reading data from various input formats (CSV, Excel, JSON, API).</li>
 *   <li>Transforming raw or typed data into a unified format using either direct mapping or model-based conversion.</li>
 *   <li>Processing and validating data records.</li>
 *   <li>Writing final records to the database.</li>
 *   <li>Tracking audit metrics for success/failure counts, execution time, etc.</li>
 * </ul>
 *
 * <p>Supports both normal and <b>bitemporal</b> processing flows, including versioning and record invalidation logic where applicable.</p>
 *
 * <p><b>Supported Execution Flows:</b></p>
 *
 * <ol>
 *   <li>
 *     <b>DataRecord-based loading:</b><br>
 *     <code>
 *     CSV/Excel/JSON/API → DataLoader → Stream&lt;DataRecord&gt; → DataProcessor → DatabaseWriter → Database
 *     </code><br>
 *     Used for direct raw data transformation and writing.
 *   </li>
 *   <li>
 *     <b>Model-based loading with custom DTOs:</b><br>
 *     <code>
 *     CSV/Excel/JSON/API → ModelLoader&lt;T&gt; → Stream&lt;T&gt; → ModelConverter → List&lt;T&gt; models
 *     </code><br>
 *     For cases where typed domain models (DTOs) are preferred over raw records.
 *   </li>
 *   <li>
 *     <b>Model-to-database transformation:</b><br>
 *     <code>
 *     List&lt;T&gt; models → ModelConverter → Stream&lt;DataRecord&gt; → DataProcessor → DatabaseWriter → Database
 *     </code><br>
 *     When data is already modeled and needs final conversion and storage.
 *   </li>
 * </ol>
 *
 * <p>This orchestrator unifies the handling of different loading strategies while ensuring consistent auditing and error handling.</p>
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
    private final ModelLoader<?> modelLoader;
    private final ModelConverter modelConverter;

    public DataOrchestrator(DataSourcingFactory dataSourceFactory,
                            DataProcessor dataProcessor,
                            DatabaseWriter databaseWriter,
                            DataLoaderConfiguration dataLoaderConfiguration, ModelLoader<?> modelLoader, ModelConverter modelConverter) {
        this.dataSourceFactory = dataSourceFactory;
        this.dataProcessor = dataProcessor;
        this.databaseWriter = databaseWriter;
        this.dataLoaderConfiguration = dataLoaderConfiguration;
        this.modelLoader = modelLoader;
        this.modelConverter = modelConverter;
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
     * <pre>
     * Internal pipeline executor shared between bitemporal and non-bitemporal runs.
     * Supports
     *      * Flow 1: Traditional data loading to database
     *      * CSV/Excel/JSON/API → DataLoader → Stream<DataRecord> → DataProcessor → DatabaseWriter → Database
     * </pre>
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

    /**
     * Flow 2: Model-based loading
     * CSV/Excel/JSON/API → ModelLoader<T> → Stream<T> → ModelConverter -> List<T> models
     */
    @SuppressWarnings("unchecked")
    public <T> ModelLoadingResult<T> executeModelLoading(String dataSourceName, Class<T> modelClass) {
        log.info("Starting model-based loading for source: {} with model type: {}",
                dataSourceName, modelClass.getSimpleName());

        try {
            DataLoaderConfiguration.DataSourceDefinition config = dataLoaderConfiguration.dataSources().get(dataSourceName);
            if (config == null) {
                throw new IllegalArgumentException("Data source not found: " + dataSourceName);
            }

            if (!"model".equals(config.target().type())) {
                log.warn("Data source {} is not configured for model loading", dataSourceName);
                throw new IllegalArgumentException(
                        "Data source is not configured for model loading. Target type must be 'model'.");
            }

            // Validate model class matches configuration
            validateModelClass(config, modelClass);

            // Load models using ModelLoader
            ModelLoader<T> typedModelLoader = (ModelLoader<T>) modelLoader;
            ModelLoadingResult<T> result = typedModelLoader.loadModels(modelClass, config);

            log.info("Completed model-based loading for source: {} with {} models loaded",
                    dataSourceName, result.models().size());

            return result;

        } catch (Exception e) {
            log.error("Error in model-based loading for source: {} with model type: {}",
                    dataSourceName, modelClass.getSimpleName(), e);

            return ModelLoadingResult.failure(
                    modelClass.getSimpleName(),
                    e.getMessage(),
                    0, 0, 0, 0,
                    List.of(e.getMessage())
            );
        }
    }

    /**
     * Flow 3: Model to database loading
     * List<T> Models → ModelConverter → Stream<DataRecord> → DataProcessor → DatabaseWriter → Database
     */
    public <T> ExecutionResult loadModelsIntoDB(String dataSourceName, List<T> models) {
        LocalDateTime startTime = LocalDateTime.now();

        try {
            log.info("Starting model-to-database loading for source: {} with {} models",
                    dataSourceName, models.size());

            DataLoaderConfiguration.DataSourceDefinition config = dataLoaderConfiguration.dataSources().get(dataSourceName);
            if (config == null) {
                throw new IllegalArgumentException("Data source not found: " + dataSourceName);
            }

            if (models.isEmpty()) {
                log.warn("No models provided for database loading");
                return ExecutionResult.success(dataSourceName, 0, 0, startTime, LocalDateTime.now(),
                        new LoadingStats(0, 0, 0, 0, 0));
            }

            // Convert models to DataRecord stream
            Stream<DataRecord> dataStream = modelConverter.convertFromModels(models, config);

            log.info("Loaded {} data records for source: {}", models.size(), dataSourceName);

            // Process data using existing processor
            var processedStream = dataProcessor.processData(dataStream, config);

            // Write to database using existing writer
            LoadingStats stats = databaseWriter.writeData(processedStream, config);
            LocalDateTime endTime = LocalDateTime.now();

            log.info("Completed model-to-database loading for source: {} with {} models in {} ms",
                    dataSourceName, models.size(), stats.writeTimeMs());

            return ExecutionResult.success(dataSourceName, models.size(),
                    (int) stats.recordsPerSecond(), startTime, endTime, stats);

        } catch (Exception e) {
            LocalDateTime endTime = LocalDateTime.now();
            log.error("Error in model-to-database loading for source: {}", dataSourceName, e);

            return ExecutionResult.failure(dataSourceName, List.of(e.getMessage()),
                    startTime, endTime);
        }
    }

    /**
     * Validates that the model class matches the configuration
     */
    private <T> void validateModelClass(DataLoaderConfiguration.DataSourceDefinition config, Class<T> modelClass) {
        log.info("Validating model-class: {}", modelClass.getSimpleName());
        String configuredClassName = config.model().className();
        String actualClassName = modelClass.getSimpleName();

        if (!configuredClassName.equals(actualClassName) &&
                !configuredClassName.equals(modelClass.getName())) {

            log.warn("Model class mismatch. Configured: {}, Actual: {}",
                    configuredClassName, actualClassName);

            // This is a warning, not an error, to allow flexibility
        }

        log.debug("Model class validation passed for: {}", actualClassName);
    }
}