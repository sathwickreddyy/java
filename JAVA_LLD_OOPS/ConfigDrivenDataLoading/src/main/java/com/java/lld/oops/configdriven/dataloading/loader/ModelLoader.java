package com.java.lld.oops.configdriven.dataloading.loader;

import com.java.lld.oops.configdriven.dataloading.config.DataLoaderConfiguration;
import com.java.lld.oops.configdriven.dataloading.exception.ModelLoadingException;
import com.java.lld.oops.configdriven.dataloading.factory.DataSourcingFactory;
import com.java.lld.oops.configdriven.dataloading.model.DataRecord;
import com.java.lld.oops.configdriven.dataloading.model.ModelLoadingResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

/**
 * Generic model loader that can load data from various sources into DTO objects with comprehensive
 * error handling, performance monitoring, and flexible configuration support.
 *
 * <p>This loader serves as the entry point for model-based data loading operations, orchestrating
 * the complete pipeline from data source reading to model object creation. It provides a unified
 * interface for loading data into strongly-typed model objects regardless of the underlying
 * data source type.</p>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *     <li><b>Multi-Source Support:</b> Works with CSV, Excel, JSON, API, and database sources</li>
 *     <li><b>Type Safety:</b> Compile-time type checking with generic type parameters</li>
 *     <li><b>Error Resilience:</b> Comprehensive error handling with detailed reporting</li>
 *     <li><b>Performance Monitoring:</b> Built-in timing and throughput metrics</li>
 *     <li><b>Configuration Validation:</b> Thorough validation of model loading configuration</li>
 * </ul>
 *
 * <p><b>Loading Process:</b></p>
 * <ol>
 *     <li>Validate configuration for model loading compatibility</li>
 *     <li>Load raw data using appropriate data loader</li>
 *     <li>Convert raw data to model objects using ModelConverter</li>
 *     <li>Apply validation and error handling</li>
 *     <li>Return comprehensive result with statistics</li>
 * </ol>
 *
 * <p><b>Java 11 Compatibility:</b></p>
 * <ul>
 *     <li>Uses traditional getter methods instead of record accessors</li>
 *     <li>Compatible with Java 11, 17, and 21</li>
 *     <li>No behavioral differences across Java versions</li>
 *     <li>Maintains performance characteristics across Java versions</li>
 * </ul>
 *
 * @param <T> The model type to load data into
 * @author sathwick
 * @since 1.0.0
 */
@Slf4j
@Component
public class ModelLoader<T> {

    private final DataSourcingFactory dataSourceFactory;
    private final ModelConverter modelConverter;

    /**
     * Constructs a new ModelLoader with required dependencies.
     *
     * @param dataSourceFactory factory for creating appropriate data loaders
     * @param modelConverter converter for transforming data records to model objects
     */
    public ModelLoader(DataSourcingFactory dataSourceFactory, ModelConverter modelConverter) {
        this.dataSourceFactory = dataSourceFactory;
        this.modelConverter = modelConverter;

        log.debug("ModelLoader initialized with data source factory and model converter");
    }

    /**
     * Loads data from configured source into a stream of model objects with comprehensive
     * error tracking and performance monitoring.
     *
     * <p>This method orchestrates the complete model loading pipeline:</p>
     * <ol>
     *     <li><b>Configuration Validation:</b> Ensures configuration is suitable for model loading</li>
     *     <li><b>Data Loading:</b> Uses appropriate data loader based on source type</li>
     *     <li><b>Model Conversion:</b> Transforms raw data into typed model objects</li>
     *     <li><b>Error Collection:</b> Gathers and reports all conversion errors</li>
     *     <li><b>Result Generation:</b> Creates comprehensive result with statistics</li>
     * </ol>
     *
     * <p><b>Performance Monitoring:</b></p>
     * <ul>
     *     <li>Tracks total execution time</li>
     *     <li>Counts successful and failed conversions</li>
     *     <li>Provides throughput metrics</li>
     *     <li>Logs performance statistics</li>
     * </ul>
     *
     * <p><b>Error Handling:</b></p>
     * <ul>
     *     <li>Configuration errors result in immediate failure</li>
     *     <li>Data loading errors are propagated with context</li>
     *     <li>Model conversion errors are collected and reported</li>
     *     <li>Partial success scenarios are properly handled</li>
     * </ul>
     *
     * @param modelClass The class type to convert data into
     * @param config The data source configuration containing loading parameters
     * @return ModelLoadingResult containing the loaded models and comprehensive metadata
     * @throws IllegalArgumentException if parameters are null or configuration is invalid
     */
    public ModelLoadingResult<T> loadModels(Class<T> modelClass, DataLoaderConfiguration.DataSourceDefinition config) {
        if (modelClass == null) {
            throw new ModelLoadingException(
                    "Model class cannot be null for model loading. Please specify a valid target model class.");
        }

        if (config == null) {
            throw new ModelLoadingException(
                    "Configuration cannot be null for model loading. Please provide a valid DataSourceDefinition configuration.");
        }

        log.info("Starting model loading for type: {} from source: {}",
                modelClass.getSimpleName(), config.getType());

        long startTime = System.currentTimeMillis();

        try {
            // Validate configuration for model loading
            validateModelConfiguration(config, modelClass);

            // Load raw data using existing data loaders
            DataLoader dataLoader = dataSourceFactory.getLoader(config.getType());
            Stream<DataRecord> dataStream = dataLoader.loadData(config);

            log.debug("Raw data loaded successfully, starting model conversion");

            // Convert to model objects with error tracking
            ModelConverter.ModelConversionResult<T> conversionResult =
                    modelConverter.convertToModelsWithErrorTracking(dataStream, modelClass, config);

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Determine success based on whether we have any successful conversions
            boolean success = conversionResult.getSuccessfulRecords() > 0;

            log.info("Model loading completed. Total: {}, Successful: {}, Errors: {} in {} ms",
                    conversionResult.getTotalRecords(),
                    conversionResult.getSuccessfulRecords(),
                    conversionResult.getErrorRecords(),
                    duration);

            return ModelLoadingResult.success(
                    modelClass.getSimpleName(),
                    conversionResult.getModels(),
                    conversionResult.getTotalRecords(),
                    conversionResult.getSuccessfulRecords(),
                    conversionResult.getErrorRecords(),
                    duration,
                    conversionResult.getErrors()
            );

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.error("Model loading failed for type: {} from source: {}. Error: {}",
                    modelClass.getSimpleName(), config.getType(), e.getMessage(), e);

            return ModelLoadingResult.failure(
                    modelClass.getSimpleName(),
                    e.getMessage(),
                    0, 0, 0,
                    duration,
                    List.of(e.getMessage())
            );
        }
    }

    /**
     * Validates that the configuration is suitable for model loading operations.
     *
     * <p>This method performs comprehensive validation to ensure that:</p>
     * <ul>
     *     <li>Target type is set to "model"</li>
     *     <li>Model configuration is present and complete</li>
     *     <li>Model class name is specified</li>
     *     <li>Required mapping configuration is available</li>
     * </ul>
     *
     * @param config the data source configuration to validate
     * @param modelClass the model class for additional validation context
     * @throws IllegalArgumentException if configuration is invalid for model loading
     */
    private void validateModelConfiguration(DataLoaderConfiguration.DataSourceDefinition config, Class<T> modelClass) {
        String modelClassName = modelClass.getSimpleName();

        // Validate target type
        if (!"model".equals(config.getTarget().getType())) {
            throw new ModelLoadingException(
                    String.format("Invalid target type for model loading. Expected 'model' but found '%s' for model class '%s'. " +
                                    "Please set target.type to 'model' in your configuration.",
                            config.getTarget().getType(), modelClassName));
        }

        // Validate model configuration presence
        if (config.getModel() == null) {
            throw new ModelLoadingException(
                    String.format("Model configuration is required when target type is 'model' for class '%s'. " +
                                    "Please provide model configuration with className, mappingStrategy, and other required settings.",
                            modelClassName));
        }

        // Validate model class name
        if (config.getModel().getClassName() == null || config.getModel().getClassName().trim().isEmpty()) {
            throw new ModelLoadingException(
                    String.format("Model class name is required in model configuration for target class '%s'. " +
                                    "Please specify the className property in your model configuration.",
                            modelClassName));
        }

        // Additional validation for mapping strategy
        String mappingStrategy = config.getModel().getMappingStrategy();
        if (mappingStrategy != null && !"DIRECT".equals(mappingStrategy) && !"MAPPED".equals(mappingStrategy)) {
            throw new ModelLoadingException(
                    String.format("Invalid mapping strategy '%s' for model class '%s'. " +
                                    "Supported strategies are 'DIRECT' and 'MAPPED'.",
                            mappingStrategy, modelClassName));
        }

        // Validate column mappings for MAPPED strategy
        if ("MAPPED".equals(mappingStrategy) &&
                (config.getColumnMapping() == null || config.getColumnMapping().isEmpty())) {
            throw new ModelLoadingException(
                    String.format("Column mappings are required when using 'MAPPED' strategy for model class '%s'. " +
                                    "Please provide columnMapping configuration or use 'DIRECT' strategy.",
                            modelClassName));
        }

        log.debug("Model configuration validation passed for class: {}", modelClassName);
    }
}
