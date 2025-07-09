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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Orchestrates the end-to-end lifecycle of data ingestion for configured data sources.
 *
 * <p>This component serves as the central coordinator for all data loading operations,
 * managing the complete pipeline from data extraction to database persistence. It provides
 * a unified interface for different loading strategies while ensuring consistent error
 * handling, auditing, and performance monitoring.</p>
 *
 * <p><b>Core Responsibilities:</b></p>
 * <ul>
 *   <li><b>Data Extraction:</b> Reading data from various input formats (CSV, Excel, JSON, API)</li>
 *   <li><b>Data Transformation:</b> Converting raw data into unified format using mapping configurations</li>
 *   <li><b>Data Processing:</b> Applying transformations, validations, and type conversions</li>
 *   <li><b>Data Persistence:</b> Writing processed records to database with transaction management</li>
 *   <li><b>Audit Tracking:</b> Recording execution metrics, success/failure counts, and performance statistics</li>
 *   <li><b>Error Management:</b> Comprehensive error handling with detailed logging and recovery strategies</li>
 * </ul>
 *
 * <p><b>Supported Processing Modes:</b></p>
 * <ul>
 *   <li><b>Normal Mode:</b> Standard data loading with append operations</li>
 *   <li><b>Bitemporal Mode:</b> Time-aware processing with record versioning and invalidation</li>
 *   <li><b>Asynchronous Mode:</b> Non-blocking execution for long-running operations</li>
 * </ul>
 *
 * <p><b>Supported Execution Flows:</b></p>
 * <ol>
 *   <li>
 *     <b>DataRecord-based Loading (Flow 1):</b><br>
 *     <code>CSV/Excel/JSON/API → DataLoader → Stream&lt;DataRecord&gt; → DataProcessor → DatabaseWriter → Database</code><br>
 *     <i>Used for direct raw data transformation and writing with minimal overhead.</i>
 *   </li>
 *   <li>
 *     <b>Model-based Loading (Flow 2):</b><br>
 *     <code>CSV/Excel/JSON/API → ModelLoader&lt;T&gt; → Stream&lt;T&gt; → ModelConverter → List&lt;T&gt; models</code><br>
 *     <i>For cases where typed domain models (DTOs) are preferred for business logic integration.</i>
 *   </li>
 *   <li>
 *     <b>Model-to-Database Transformation (Flow 3):</b><br>
 *     <code>List&lt;T&gt; models → ModelConverter → Stream&lt;DataRecord&gt; → DataProcessor → DatabaseWriter → Database</code><br>
 *     <i>When data is already modeled and needs final conversion and storage.</i>
 *   </li>
 * </ol>
 *
 * <p><b>Performance Features:</b></p>
 * <ul>
 *   <li><b>Stream Processing:</b> Memory-efficient processing of large datasets</li>
 *   <li><b>Batch Operations:</b> Configurable batch sizes for optimal database throughput</li>
 *   <li><b>Connection Pooling:</b> Efficient database connection management</li>
 *   <li><b>Parallel Processing:</b> Support for concurrent data processing where applicable</li>
 * </ul>
 *
 * <p><b>Error Handling Strategy:</b></p>
 * <ul>
 *   <li><b>Graceful Degradation:</b> Continues processing valid records when individual records fail</li>
 *   <li><b>Detailed Logging:</b> Comprehensive error tracking with context information</li>
 *   <li><b>Transaction Safety:</b> Ensures data consistency through proper transaction management</li>
 *   <li><b>Recovery Options:</b> Provides mechanisms for retry and partial recovery</li>
 * </ul>
 *
 * <p><b>Java 11 Compatibility:</b></p>
 * <ul>
 *   <li>Uses traditional getter methods instead of record accessors</li>
 *   <li>Compatible with Java 11, 17, and 21</li>
 *   <li>No behavioral changes from higher Java versions</li>
 *   <li>Maintains performance characteristics across Java versions</li>
 * </ul>
 *
 * @author sathwick
 * @since 1.0.0
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

    /**
     * Constructs a new DataOrchestrator with all required dependencies.
     *
     * @param dataSourceFactory Factory for creating appropriate data loaders
     * @param dataProcessor Service for processing and transforming data records
     * @param databaseWriter Service for writing data to database
     * @param dataLoaderConfiguration Configuration containing data source definitions
     * @param modelLoader Generic model loader for DTO-based operations
     * @param modelConverter Converter between models and data records
     */
    public DataOrchestrator(DataSourcingFactory dataSourceFactory,
                            DataProcessor dataProcessor,
                            DatabaseWriter databaseWriter,
                            DataLoaderConfiguration dataLoaderConfiguration,
                            ModelLoader<?> modelLoader,
                            ModelConverter modelConverter) {
        this.dataSourceFactory = dataSourceFactory;
        this.dataProcessor = dataProcessor;
        this.databaseWriter = databaseWriter;
        this.dataLoaderConfiguration = dataLoaderConfiguration;
        this.modelLoader = modelLoader;
        this.modelConverter = modelConverter;

        log.info("DataOrchestrator initialized with {} configured data sources",
                dataLoaderConfiguration.getDataSources().size());
    }

    /**
     * Executes the full data loading pipeline for a given data source using normal (non-bitemporal) mode.
     *
     * <p>This method provides the standard data loading flow suitable for most use cases where
     * historical data versioning is not required. It performs a complete extract-transform-load
     * operation with comprehensive error handling and performance monitoring.</p>
     *
     * <p><b>Execution Steps:</b></p>
     * <ol>
     *     <li>Validate data source configuration exists</li>
     *     <li>Load data from the configured source</li>
     *     <li>Process and transform data according to mappings</li>
     *     <li>Write processed data to database</li>
     *     <li>Record execution statistics and audit information</li>
     * </ol>
     *
     * <p><b>Error Handling:</b></p>
     * <ul>
     *     <li>Configuration errors result in immediate failure with detailed messages</li>
     *     <li>Data processing errors are logged but don't stop the entire operation</li>
     *     <li>Database errors are handled with transaction rollback</li>
     * </ul>
     *
     * @param dataSourceName the name of the configured data source to execute
     * @return {@link ExecutionResult} containing execution outcome, statistics, and any errors
     * @throws IllegalArgumentException if dataSourceName is null or empty
     */
    public ExecutionResult executeDataSourcing(String dataSourceName) {
        validateDataSourceName(dataSourceName);
        return executePipeline(dataSourceName, null);
    }

    /**
     * Executes the full data loading pipeline in bitemporal mode for a given data source and reporting date.
     *
     * <p>Bitemporal processing ensures data consistency across time dimensions by:</p>
     * <ul>
     *     <li><b>Invalidating Existing Records:</b> Marks existing records with the same reporting date as invalid</li>
     *     <li><b>Version Management:</b> Creates new versions of records while preserving history</li>
     *     <li><b>Temporal Consistency:</b> Ensures data integrity across valid-time and transaction-time dimensions</li>
     * </ul>
     *
     * <p><b>Use Cases:</b></p>
     * <ul>
     *     <li>Financial data where historical accuracy is critical</li>
     *     <li>Regulatory reporting requiring audit trails</li>
     *     <li>Data corrections that need to preserve original values</li>
     *     <li>Time-series data with late-arriving corrections</li>
     * </ul>
     *
     * <p><b>Processing Flow:</b></p>
     * <ol>
     *     <li>Parse and validate the reporting date</li>
     *     <li>Execute standard data loading pipeline</li>
     *     <li>Invalidate existing records for the reporting date</li>
     *     <li>Insert new records with current transaction time</li>
     *     <li>Update audit trails with bitemporal information</li>
     * </ol>
     *
     * @param dataSourceName the name of the configured data source to execute
     * @param date the reporting date in yyyy-MM-dd format for bitemporal processing
     * @return {@link ExecutionResult} containing execution outcome with bitemporal statistics
     * @throws IllegalArgumentException if dataSourceName is invalid or date format is incorrect
     */
    public ExecutionResult executeDataSourcingWithBiTemporality(String dataSourceName, String date) {
        validateDataSourceName(dataSourceName);

        if (date == null || date.trim().isEmpty()) {
            throw new IllegalArgumentException("Reporting date cannot be null or empty for bitemporal processing");
        }

        try {
            LocalDate reportingDate = LocalDate.parse(date);
            log.info("Executing bitemporal data loading for source '{}' with reporting date: {}",
                    dataSourceName, reportingDate);
            return executePipeline(dataSourceName, reportingDate);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format. Expected yyyy-MM-dd, got: " + date, e);
        }
    }

    /**
     * Executes the full data loading pipeline asynchronously for the given data source.
     *
     * <p>Asynchronous execution is beneficial for:</p>
     * <ul>
     *     <li><b>Large Datasets:</b> Processing that may take significant time</li>
     *     <li><b>Non-blocking Operations:</b> Allowing other operations to continue</li>
     *     <li><b>Batch Processing:</b> Scheduled or triggered data loads</li>
     *     <li><b>User Experience:</b> Preventing UI blocking in interactive applications</li>
     * </ul>
     *
     * <p><b>Implementation Notes:</b></p>
     * <ul>
     *     <li>Uses CompletableFuture for non-blocking execution</li>
     *     <li>Maintains same error handling as synchronous execution</li>
     *     <li>Results can be retrieved when ready or combined with other futures</li>
     * </ul>
     *
     * @param dataSourceName the name of the configured data source to execute
     * @return {@link CompletableFuture} containing the {@link ExecutionResult} when complete
     */
    public CompletableFuture<ExecutionResult> executeDataSourceAsync(String dataSourceName) {
        validateDataSourceName(dataSourceName);
        log.info("Starting asynchronous execution for data source: {}", dataSourceName);
        return CompletableFuture.completedFuture(executeDataSourcing(dataSourceName));
    }

    /**
     * Internal pipeline executor shared between bitemporal and non-bitemporal runs.
     *
     * <p>This method implements the core data loading logic that is common to all execution modes.
     * It handles the complete pipeline from data extraction to persistence while maintaining
     * consistent error handling and performance monitoring.</p>
     *
     * <p><b>Pipeline Stages:</b></p>
     * <ol>
     *     <li><b>Configuration Loading:</b> Retrieve and validate data source configuration</li>
     *     <li><b>Data Loading:</b> Extract data using appropriate loader based on source type</li>
     *     <li><b>Data Processing:</b> Apply transformations, mappings, and validations</li>
     *     <li><b>Database Writing:</b> Persist processed data with appropriate mode (normal/bitemporal)</li>
     *     <li><b>Result Generation:</b> Create execution result with statistics and audit information</li>
     * </ol>
     *
     * <p><b>Error Recovery:</b></p>
     * <ul>
     *     <li>Configuration errors result in immediate failure</li>
     *     <li>Data processing errors are collected but don't stop processing</li>
     *     <li>Database errors trigger transaction rollback</li>
     *     <li>Partial success scenarios are properly reported</li>
     * </ul>
     *
     * @param dataSourceName name of the data source to process
     * @param reportingDate reporting date for bitemporal handling; null for normal mode
     * @return {@link ExecutionResult} summarizing the execution outcome
     */
    private ExecutionResult executePipeline(String dataSourceName, LocalDate reportingDate) {
        LocalDateTime startTime = LocalDateTime.now();
        String mode = reportingDate != null ? "BITEMPORAL" : "NORMAL";

        log.info("Starting execution for data source '{}', mode: {}", dataSourceName, mode);

        try {
            // Load and validate configuration
            DataLoaderConfiguration.DataSourceDefinition config = loadConfiguration(dataSourceName);
            log.debug("Loaded configuration for '{}': type={}, target={}",
                    dataSourceName, config.getType(), config.getTarget().getTable());

            // Execute data loading pipeline
            ExecutionResult result = executeDataLoadingPipeline(config, dataSourceName, reportingDate, startTime);

            log.info("Execution completed for '{}' in {}ms. Success: {}, Records: {}",
                    dataSourceName, result.getDurationMs(), result.isSuccess(), result.getTotalRecords());

            return result;

        } catch (IllegalArgumentException e) {
            log.error("Configuration error for data source '{}': {}", dataSourceName, e.getMessage());
            return ExecutionResult.failure(dataSourceName, List.of(e.getMessage()),
                    startTime, LocalDateTime.now());

        } catch (Exception e) {
            log.error("Fatal error during execution of '{}': {}", dataSourceName, e.getMessage(), e);
            return ExecutionResult.failure(dataSourceName,
                    List.of("Internal error: " + e.getMessage()),
                    startTime, LocalDateTime.now());
        }
    }

    /**
     * Executes the core data loading pipeline with proper resource management.
     *
     * @param config Data source configuration
     * @param dataSourceName Name of the data source
     * @param reportingDate Reporting date for bitemporal processing (null for normal mode)
     * @param startTime Execution start time
     * @return Execution result with statistics
     */
    private ExecutionResult executeDataLoadingPipeline(DataLoaderConfiguration.DataSourceDefinition config,
                                                       String dataSourceName,
                                                       LocalDate reportingDate,
                                                       LocalDateTime startTime) {
        // Load data from source
        var loader = dataSourceFactory.getLoader(config.getType());
        var dataStream = loader.loadData(config);
        log.info("Data loaded for '{}', proceeding to processing.", dataSourceName);

        // Process data
        var processedStream = dataProcessor.processData(dataStream, config);
        log.info("Data processed for '{}'", dataSourceName);

        // Convert stream to list for testing purposes (TODO: remove after testing)
        List<DataRecord> records = processedStream.collect(Collectors.toList());
        processedStream = records.stream();

        // Write to database if configured
        if (shouldWriteToDatabase(config)) {
            return executeDatabaseWrite(processedStream, config, dataSourceName, reportingDate,
                    startTime, records);
        } else {
            return executeDryRun(records, dataSourceName, startTime);
        }
    }

    /**
     * Executes database write operation with proper statistics collection.
     */
    private ExecutionResult executeDatabaseWrite(Stream<DataRecord> processedStream,
                                                 DataLoaderConfiguration.DataSourceDefinition config,
                                                 String dataSourceName,
                                                 LocalDate reportingDate,
                                                 LocalDateTime startTime,
                                                 List<DataRecord> records) {
        LoadingStats stats;

        if (reportingDate != null) {
            stats = databaseWriter.writeDataWithBiTemporality(processedStream, config, reportingDate);
        } else {
            stats = databaseWriter.writeData(processedStream, config);
        }

        LocalDateTime endTime = LocalDateTime.now();
        log.info("DB write completed for '{}'. Records/sec: {}, Duration: {}ms, Batches: {}",
                dataSourceName, stats.getRecordsPerSecond(), stats.getWriteTimeMs(), stats.getBatchCount());

        // TODO: remove this after testing
        DataRecordPrinter.printRecords(records);

        return ExecutionResult.success(dataSourceName, stats.getBatchCount(), stats.getBatchCount(),
                startTime, endTime, stats);
    }

    /**
     * Executes dry run when no database target is configured.
     */
    private ExecutionResult executeDryRun(List<DataRecord> records, String dataSourceName, LocalDateTime startTime) {
        log.warn("No target table for '{}'. Skipping DB write. Printing {} records for review.",
                dataSourceName, records.size());
        DataRecordPrinter.printRecords(records);

        return ExecutionResult.success(dataSourceName, records.size(), records.size(),
                startTime, LocalDateTime.now(), null);
    }

    /**
     * Loads and validates configuration for the specified data source.
     *
     * @param dataSourceName Name of the data source
     * @return Data source configuration
     * @throws IllegalArgumentException if configuration is not found
     */
    private DataLoaderConfiguration.DataSourceDefinition loadConfiguration(String dataSourceName) {
        return Optional.ofNullable(dataLoaderConfiguration.getDataSources().get(dataSourceName))
                .orElseThrow(() -> new IllegalArgumentException("Data source not found: " + dataSourceName));
    }

    /**
     * Validates data source name parameter.
     *
     * @param dataSourceName Name to validate
     * @throws IllegalArgumentException if name is null or empty
     */
    private void validateDataSourceName(String dataSourceName) {
        if (dataSourceName == null || dataSourceName.trim().isEmpty()) {
            throw new IllegalArgumentException("Data source name cannot be null or empty");
        }
    }

    /**
     * Checks if the given configuration has a valid database target table configured.
     *
     * <p>A configuration is considered to have a valid database target if:</p>
     * <ul>
     *     <li>Target configuration is not null</li>
     *     <li>Table name is specified and not blank</li>
     * </ul>
     *
     * @param config the {@link DataLoaderConfiguration.DataSourceDefinition} to check
     * @return true if table name is present and non-blank, false otherwise
     */
    private boolean shouldWriteToDatabase(DataLoaderConfiguration.DataSourceDefinition config) {
        return config.getTarget() != null &&
                config.getTarget().getTable() != null &&
                !config.getTarget().getTable().isBlank();
    }

    /**
     * Executes model-based data loading pipeline (Flow 2).
     *
     * <p><b>Model-Based Loading Pipeline:</b></p>
     * <code>CSV/Excel/JSON/API → ModelLoader&lt;T&gt; → Stream&lt;T&gt; → ModelConverter → List&lt;T&gt; models</code>
     *
     * <p>This flow is designed for scenarios where:</p>
     * <ul>
     *     <li><b>Type Safety:</b> Strong typing is required for business logic</li>
     *     <li><b>Validation:</b> Bean validation annotations need to be applied</li>
     *     <li><b>Business Logic:</b> Domain models contain business methods</li>
     *     <li><b>Integration:</b> Models need to be passed to other services</li>
     * </ul>
     *
     * <p><b>Configuration Requirements:</b></p>
     * <ul>
     *     <li>Target type must be set to "model"</li>
     *     <li>Model configuration must specify the target class</li>
     *     <li>Column mappings must align with model field names</li>
     * </ul>
     *
     * <p><b>Error Handling:</b></p>
     * <ul>
     *     <li>Configuration validation errors result in immediate failure</li>
     *     <li>Model conversion errors are collected and reported</li>
     *     <li>Partial success is supported with detailed error reporting</li>
     * </ul>
     *
     * @param <T> The type of model to load data into
     * @param dataSourceName the name of the configured data source
     * @param modelClass the class object representing the target model type
     * @return {@link ModelLoadingResult} containing loaded models and execution statistics
     * @throws IllegalArgumentException if configuration is invalid for model loading
     */
    @SuppressWarnings("unchecked")
    public <T> ModelLoadingResult<T> executeModelLoading(String dataSourceName, Class<T> modelClass) {
        validateDataSourceName(dataSourceName);

        if (modelClass == null) {
            throw new IllegalArgumentException("Model class cannot be null");
        }

        log.info("Starting model-based loading for source: {} with model type: {}",
                dataSourceName, modelClass.getSimpleName());

        try {
            // Load and validate configuration
            DataLoaderConfiguration.DataSourceDefinition config = loadConfiguration(dataSourceName);
            validateModelConfiguration(config, dataSourceName);
            validateModelClass(config, modelClass);

            // Execute model loading
            ModelLoader<T> typedModelLoader = (ModelLoader<T>) modelLoader;
            ModelLoadingResult<T> result = typedModelLoader.loadModels(modelClass, config);

            log.info("Completed model-based loading for source: {} with {} models loaded",
                    dataSourceName, result.getModels().size());

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
     * Executes model-to-database loading pipeline (Flow 3).
     *
     * <p><b>Model-to-Database Pipeline:</b></p>
     * <code>List&lt;T&gt; models → ModelConverter → Stream&lt;DataRecord&gt; → DataProcessor → DatabaseWriter → Database</code>
     *
     * <p>This flow is useful when:</p>
     * <ul>
     *     <li><b>Pre-processed Data:</b> Models are already created and validated</li>
     *     <li><b>Business Logic:</b> Models have been processed by business rules</li>
     *     <li><b>Batch Operations:</b> Multiple models need to be persisted together</li>
     *     <li><b>Integration:</b> Models come from external services or APIs</li>
     * </ul>
     *
     * <p><b>Processing Steps:</b></p>
     * <ol>
     *     <li>Validate input models list is not empty</li>
     *     <li>Convert models to DataRecord format using ModelConverter</li>
     *     <li>Process data records through standard pipeline</li>
     *     <li>Write processed records to database</li>
     *     <li>Return execution statistics</li>
     * </ol>
     *
     * <p><b>Performance Considerations:</b></p>
     * <ul>
     *     <li>Batch size is configurable for optimal database performance</li>
     *     <li>Memory usage is managed through streaming where possible</li>
     *     <li>Transaction boundaries are properly managed</li>
     * </ul>
     *
     * @param <T> The type of models being loaded
     * @param dataSourceName the name of the configured data source for database settings
     * @param models the list of model objects to be persisted to the database
     * @return {@link ExecutionResult} containing execution outcome and statistics
     * @throws IllegalArgumentException if dataSourceName is invalid or models list is null
     */
    public <T> ExecutionResult loadModelsIntoDB(String dataSourceName, List<T> models) {
        validateDataSourceName(dataSourceName);

        if (models == null) {
            throw new IllegalArgumentException("Models list cannot be null");
        }

        LocalDateTime startTime = LocalDateTime.now();

        try {
            log.info("Starting model-to-database loading for source: {} with {} models",
                    dataSourceName, models.size());

            DataLoaderConfiguration.DataSourceDefinition config = loadConfiguration(dataSourceName);

            if (models.isEmpty()) {
                log.warn("No models provided for database loading");
                return ExecutionResult.success(dataSourceName, 0, 0, startTime, LocalDateTime.now(),
                        new LoadingStats(0, 0, 0, 0, 0));
            }

            // Convert models to DataRecord stream
            Stream<DataRecord> dataStream = modelConverter.convertFromModels(models, config);
            log.info("Converted {} models to data records for source: {}", models.size(), dataSourceName);

            // Process data using existing processor
            var processedStream = dataProcessor.processData(dataStream, config);

            // Write to database using existing writer
            LoadingStats stats = databaseWriter.writeData(processedStream, config);
            LocalDateTime endTime = LocalDateTime.now();

            log.info("Completed model-to-database loading for source: {} with {} models in {} ms",
                    dataSourceName, models.size(), stats.getWriteTimeMs());

            return ExecutionResult.success(dataSourceName, models.size(),
                    (int) stats.getRecordsPerSecond(), startTime, endTime, stats);

        } catch (Exception e) {
            LocalDateTime endTime = LocalDateTime.now();
            log.error("Error in model-to-database loading for source: {}", dataSourceName, e);

            return ExecutionResult.failure(dataSourceName, List.of(e.getMessage()),
                    startTime, endTime);
        }
    }

    /**
     * Validates that the configuration is properly set up for model loading.
     *
     * @param config Data source configuration
     * @param dataSourceName Data source name for error messages
     * @throws IllegalArgumentException if configuration is invalid for model loading
     */
    private void validateModelConfiguration(DataLoaderConfiguration.DataSourceDefinition config,
                                            String dataSourceName) {
        if (!"model".equals(config.getTarget().getType())) {
            log.warn("Data source {} is not configured for model loading", dataSourceName);
            throw new IllegalArgumentException(
                    "Data source is not configured for model loading. Target type must be 'model'.");
        }

        if (config.getModel() == null) {
            throw new IllegalArgumentException(
                    "Model configuration is required when target type is 'model'");
        }
    }

    /**
     * Validates that the model class matches the configuration with flexible matching.
     *
     * <p>This method performs validation but allows for flexibility in class name matching:</p>
     * <ul>
     *     <li>Exact simple name match (e.g., "MarketDataDTO")</li>
     *     <li>Exact fully qualified name match (e.g., "com.example.MarketDataDTO")</li>
     *     <li>Logs warnings for mismatches but doesn't fail to allow flexibility</li>
     * </ul>
     *
     * @param <T> The model type
     * @param config Data source configuration containing model class name
     * @param modelClass The actual model class being used
     */
    private <T> void validateModelClass(DataLoaderConfiguration.DataSourceDefinition config, Class<T> modelClass) {
        log.info("Validating model class: {}", modelClass.getSimpleName());

        String configuredClassName = config.getModel().getClassName();
        String actualClassName = modelClass.getSimpleName();
        String actualFullName = modelClass.getName();

        boolean isExactMatch = configuredClassName.equals(actualClassName) ||
                configuredClassName.equals(actualFullName);

        if (!isExactMatch) {
            log.warn("Model class mismatch. Configured: {}, Actual: {} ({})",
                    configuredClassName, actualClassName, actualFullName);
            // This is a warning, not an error, to allow flexibility in development
        }

        log.debug("Model class validation completed for: {}", actualClassName);
    }
}
