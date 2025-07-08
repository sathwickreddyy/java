package com.java.lld.oops.configdriven.dataloading.loader;

import com.java.lld.oops.configdriven.dataloading.config.DataLoaderConfiguration;
import com.java.lld.oops.configdriven.dataloading.factory.DataSourcingFactory;
import com.java.lld.oops.configdriven.dataloading.model.DataRecord;
import com.java.lld.oops.configdriven.dataloading.model.ModelLoadingResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

/**
 * Generic model loader that can load data from various sources into DTO objects
 * Supports Java 11, 17, and 21
 *
 * @param <T> The model type to load data into
 */
@Slf4j
@Component
public class ModelLoader<T> {
    
    private final DataSourcingFactory dataSourceFactory;
    private final ModelConverter modelConverter;

    public ModelLoader(DataSourcingFactory dataSourceFactory, ModelConverter modelConverter) {
        this.dataSourceFactory = dataSourceFactory;
        this.modelConverter = modelConverter;
    }

    /**
     * Loads data from configured source into a stream of model objects
     *
     * @param modelClass The class type to convert data into
     * @param config The data source configuration
     * @return ModelLoadingResult containing the loaded models and metadata
     */
    public ModelLoadingResult<T> loadModels(Class<T> modelClass, DataLoaderConfiguration.DataSourceDefinition config) {
        log.info("Starting model loading for type: {} from source: {}",
                modelClass.getSimpleName(), config.type());

        long startTime = System.currentTimeMillis();
        int totalRecords = 0;
        int successfulRecords = 0;
        int errorRecords = 0;

        try {
            // Validate configuration for model loading
            validateModelConfiguration(config, modelClass);

            // Load raw data using existing data loaders
            DataLoader dataLoader = dataSourceFactory.getLoader(config.type());
            Stream<DataRecord> dataStream = dataLoader.loadData(config);

            log.debug("Raw data loaded successfully, starting model conversion");

            // Convert to model objects
            Stream<T> modelStream = modelConverter.convertToModels(dataStream, modelClass, config);

            // Collect results and count records
            List<T> models = modelStream.collect(java.util.stream.Collectors.toList());

            successfulRecords = models.size();
            totalRecords = successfulRecords; // For now, assuming all loaded records are successful

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.info("Model loading completed successfully. Loaded {} models of type {} in {} ms",
                    successfulRecords, modelClass.getSimpleName(), duration);

            return ModelLoadingResult.success(
                    modelClass.getSimpleName(),
                    models,
                    totalRecords,
                    successfulRecords,
                    errorRecords,
                    duration
            );

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.error("Model loading failed for type: {} from source: {}. Error: {}",
                    modelClass.getSimpleName(), config.type(), e.getMessage(), e);

            return ModelLoadingResult.failure(
                    modelClass.getSimpleName(),
                    e.getMessage(),
                    totalRecords,
                    successfulRecords,
                    errorRecords,
                    duration
            );
        }
    }

    /**
     * Validates that the configuration is suitable for model loading
     */
    private void validateModelConfiguration(DataLoaderConfiguration.DataSourceDefinition config, Class<T> modelClass) {
        if (!"model".equals(config.target().type())) {
            throw new IllegalArgumentException(
                    String.format("Target type must be 'model' for model loading. Found: %s",
                            config.target().type()));
        }

        if (config.model() == null) {
            throw new IllegalArgumentException("Model configuration is required when target type is 'model'");
        }

        if (config.model().className() == null || config.model().className().trim().isEmpty()) {
            throw new IllegalArgumentException("Model class name is required in model configuration");
        }

        log.debug("Model configuration validation passed for class: {}", modelClass.getSimpleName());
    }
}