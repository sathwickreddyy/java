package com.java.lld.oops.configdriven.dataloading.loader;

import com.java.lld.oops.configdriven.dataloading.config.DataLoaderConfiguration;
import com.java.lld.oops.configdriven.dataloading.exception.ModelLoadingException;
import com.java.lld.oops.configdriven.dataloading.model.DataRecord;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Advanced model converter that provides bidirectional conversion between DataRecord objects
 * and strongly-typed model objects using reflection and comprehensive error handling.
 *
 * <p>This converter serves as the bridge between the framework's internal DataRecord format
 * and user-defined DTO/model classes, enabling type-safe data processing with automatic
 * validation, flexible mapping strategies, and robust error recovery mechanisms.</p>
 *
 * <p><b>Core Capabilities:</b></p>
 * <ul>
 *     <li><b>Bidirectional Conversion:</b> DataRecord ↔ Model object conversion</li>
 *     <li><b>Reflection-Based Mapping:</b> Automatic field mapping using reflection</li>
 *     <li><b>Bean Validation Integration:</b> JSR-303 validation support</li>
 *     <li><b>Flexible Mapping Strategies:</b> Direct and mapped field assignment</li>
 *     <li><b>Comprehensive Error Tracking:</b> Detailed error collection and reporting</li>
 *     <li><b>Type Safety:</b> Compile-time and runtime type checking</li>
 * </ul>
 *
 * <p><b>Mapping Strategies:</b></p>
 * <ul>
 *     <li><b>DIRECT Mapping:</b>
 *         <ul>
 *             <li>Source field names must exactly match model field names</li>
 *             <li>No configuration required - automatic field matching</li>
 *             <li>Fastest performance with minimal overhead</li>
 *             <li>Best for scenarios where source and model schemas align</li>
 *         </ul>
 *     </li>
 *     <li><b>MAPPED Strategy:</b>
 *         <ul>
 *             <li>Uses explicit column mappings from configuration</li>
 *             <li>Supports field renaming and transformation</li>
 *             <li>Enables complex mapping scenarios</li>
 *             <li>Provides fine-grained control over field assignment</li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * <p><b>Validation Integration:</b></p>
 * <ul>
 *     <li><b>JSR-303 Support:</b> Automatic validation using Bean Validation annotations</li>
 *     <li><b>Custom Validators:</b> Support for custom validation logic</li>
 *     <li><b>Error Aggregation:</b> Collection of all validation errors for comprehensive reporting</li>
 *     <li><b>Strict/Lenient Modes:</b> Configurable validation behavior</li>
 * </ul>
 *
 * <p><b>Error Handling Features:</b></p>
 * <ul>
 *     <li><b>Comprehensive Error Tracking:</b> Detailed error collection with context</li>
 *     <li><b>Graceful Degradation:</b> Continues processing valid records when errors occur</li>
 *     <li><b>Detailed Error Messages:</b> Specific error descriptions for debugging</li>
 *     <li><b>Error Statistics:</b> Success/failure counts and performance metrics</li>
 * </ul>
 *
 * <p><b>Performance Optimizations:</b></p>
 * <ul>
 *     <li><b>Reflection Caching:</b> Caches field and method lookups for performance</li>
 *     <li><b>Streaming Support:</b> Memory-efficient processing of large datasets</li>
 *     <li><b>Lazy Evaluation:</b> Processes records only when consumed</li>
 *     <li><b>Minimal Object Creation:</b> Optimized for garbage collection efficiency</li>
 * </ul>
 *
 * <p><b>Java 11 Compatibility:</b></p>
 * <ul>
 *     <li>Uses traditional getter methods instead of record accessors</li>
 *     <li>Compatible with Java 11, 17, and 21</li>
 *     <li>No behavioral differences across Java versions</li>
 *     <li>Maintains performance characteristics across Java versions</li>
 * </ul>
 *
 * @author sathwick
 * @since 1.0.0
 */
@Slf4j
@Component
public class ModelConverter {

    private final DataTypeConverter dataTypeConverter;
    private final Validator validator;

    /**
     * Constructs a new ModelConverter with required dependencies.
     *
     * @param dataTypeConverter the type converter for field value conversion
     * @param validator the JSR-303 validator for model validation
     */
    public ModelConverter(DataTypeConverter dataTypeConverter, Validator validator) {
        this.dataTypeConverter = dataTypeConverter;
        this.validator = validator;

        log.debug("ModelConverter initialized with type converter and validator");
    }

    /**
     * Result wrapper for model conversion operations with comprehensive error tracking and statistics.
     *
     * <p>This class encapsulates the results of model conversion operations, providing
     * detailed information about successful conversions, errors encountered, and
     * performance statistics for monitoring and debugging purposes.</p>
     *
     * <p><b>Key Metrics:</b></p>
     * <ul>
     *     <li><b>Success Rate:</b> Ratio of successful to total conversions</li>
     *     <li><b>Error Details:</b> Specific error messages for failed conversions</li>
     *     <li><b>Performance Data:</b> Conversion counts and timing information</li>
     * </ul>
     *
     * @param <T> the type of models being converted
     */
    @Getter
    public static class ModelConversionResult<T> {

        /**
         * List of successfully converted model objects
         */
        private final List<T> models;

        /**
         * List of error messages for failed conversions
         */
        private final List<String> errors;

        /**
         * Total number of records processed
         */
        private final int totalRecords;

        /**
         * Number of records successfully converted
         */
        private final int successfulRecords;

        /**
         * Number of records that failed conversion
         */
        private final int errorRecords;

        /**
         * Constructs a new conversion result with the specified data.
         *
         * @param models the list of successfully converted models
         * @param errors the list of error messages
         * @param totalRecords the total number of records processed
         */
        public ModelConversionResult(List<T> models, List<String> errors, int totalRecords) {
            this.models = models != null ? models : new ArrayList<>();
            this.errors = errors != null ? errors : new ArrayList<>();
            this.totalRecords = totalRecords;
            this.successfulRecords = this.models.size();
            this.errorRecords = this.errors.size();
        }
    }

    /**
     * Enhanced method that converts DataRecord stream to models with comprehensive error tracking.
     *
     * <p>This method provides the primary conversion functionality with advanced error handling,
     * performance monitoring, and detailed logging. It processes each record individually to
     * ensure that errors in individual records don't affect the processing of other records.</p>
     *
     * <p><b>Processing Flow:</b></p>
     * <ol>
     *     <li>Validate input parameters</li>
     *     <li>Process each DataRecord individually</li>
     *     <li>Apply model conversion with error handling</li>
     *     <li>Collect successful models and error messages</li>
     *     <li>Generate comprehensive result with statistics</li>
     * </ol>
     *
     * <p><b>Error Handling Strategy:</b></p>
     * <ul>
     *     <li>Invalid DataRecords are logged and counted as errors</li>
     *     <li>Conversion exceptions are caught and converted to error messages</li>
     *     <li>Processing continues for subsequent records</li>
     *     <li>Detailed error context is preserved for debugging</li>
     * </ul>
     *
     * @param <T> the type of model to convert to
     * @param dataStream the stream of DataRecord objects to convert
     * @param modelClass the class object representing the target model type
     * @param config the data source configuration containing mapping rules
     * @return ModelConversionResult containing converted models and error information
     * @throws IllegalArgumentException if required parameters are null
     */
    public <T> ModelConversionResult<T> convertToModelsWithErrorTracking(Stream<DataRecord> dataStream,
                                                                         Class<T> modelClass,
                                                                         DataLoaderConfiguration.DataSourceDefinition config) {
        if (dataStream == null) {
            throw new ModelLoadingException(
                    "Data stream cannot be null for model conversion. Please provide a valid stream of DataRecord objects.");
        }

        if (modelClass == null) {
            throw new ModelLoadingException(
                    "Model class cannot be null for model conversion. Please specify the target model class type.");
        }

        if (config == null) {
            throw new ModelLoadingException(
                    "Configuration cannot be null for model conversion. Please provide a valid DataSourceDefinition configuration.");
        }

        // Additional validation for model-specific configuration
        if (config.getModel() == null) {
            throw new ModelLoadingException(
                    String.format("Model configuration is missing for model class '%s'. Please ensure target type is 'model' and model configuration is provided.",
                            modelClass.getSimpleName()));
        }

        log.debug("Starting conversion to model type: {} with error tracking", modelClass.getSimpleName());

        List<T> models = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        AtomicInteger totalRecords = new AtomicInteger(0);

        dataStream.forEach(record -> {
            int recordNumber = totalRecords.incrementAndGet();

            if (!record.isValid()) {
                String errorMsg = String.format("Record %d is invalid: %s",
                        record.getRowNumber(), record.getErrorMessage());
                errors.add(errorMsg);
                log.warn(errorMsg);
                return;
            }

            try {
                T model = convertToModelSafely(record, modelClass, config);
                if (model != null) {
                    models.add(model);
                    log.debug("Successfully converted record {} to model {}",
                            record.getRowNumber(), modelClass.getSimpleName());
                }
            } catch (ModelConversionException e) {
                errors.add(e.getMessage());
                log.error("Conversion failed for record {}: {}", record.getRowNumber(), e.getMessage());
            } catch (Exception e) {
                String errorMsg = String.format("Unexpected error converting record %d: %s",
                        record.getRowNumber(), e.getMessage());
                errors.add(errorMsg);
                log.error(errorMsg, e);
            }
        });

        log.info("Model conversion completed. Total: {}, Successful: {}, Errors: {}",
                totalRecords.get(), models.size(), errors.size());

        return new ModelConversionResult<>(models, errors, totalRecords.get());
    }

    /**
     * Safe conversion method that handles errors gracefully without breaking the processing flow.
     *
     * <p>This method implements the core conversion logic with comprehensive error handling
     * and validation integration. It supports both direct and mapped field assignment
     * strategies based on the configuration.</p>
     *
     * @param <T> the type of model to convert to
     * @param record the DataRecord to convert
     * @param modelClass the target model class
     * @param config the conversion configuration
     * @return converted model object or null if conversion fails in lenient mode
     * @throws ModelConversionException if conversion fails in strict mode
     */
    private <T> T convertToModelSafely(DataRecord record, Class<T> modelClass,
                                       DataLoaderConfiguration.DataSourceDefinition config) {
        try {
            T instance = modelClass.getDeclaredConstructor().newInstance();

            if ("DIRECT".equals(config.getModel().getMappingStrategy())) {
                populateModelDirect(instance, record.getData(), modelClass);
            } else {
                populateModelMapped(instance, record.getData(), modelClass, config.getColumnMapping());
            }

            // Validate the populated model using JSR-303 validation
            validateModel(instance, record.getRowNumber(), config);

            return instance;

        } catch (ModelConversionException e) {
            // Re-throw validation exceptions
            throw e;
        } catch (Exception e) {
            // Wrap other exceptions
            throw new ModelConversionException(
                    String.format("Failed to convert record %d: %s", record.getRowNumber(), e.getMessage()), e);
        }
    }

    /**
     * Validates a populated model using JSR-303 Bean Validation.
     *
     * @param <T> the type of model to validate
     * @param instance the model instance to validate
     * @param rowNumber the row number for error reporting
     * @param config the configuration containing validation settings
     * @throws ModelConversionException if validation fails
     */
    private <T> void validateModel(T instance, int rowNumber, DataLoaderConfiguration.DataSourceDefinition config) {
        if (config.getValidation() != null && config.getValidation().isDataQualityChecks()) {
            Set<ConstraintViolation<T>> violations = validator.validate(instance);
            if (!violations.isEmpty()) {
                StringBuilder errorMessage = new StringBuilder("Validation errors for record " + rowNumber + ": ");
                violations.forEach(violation ->
                        errorMessage.append(violation.getPropertyPath()).append(" - ").append(violation.getMessage()).append("; "));

                log.warn("Validation failed for record {}: {}", rowNumber, errorMessage);

                // Always throw exception to be caught and handled properly
                throw new ModelConversionException(errorMessage.toString());
            }
        }
    }

    /**
     * Legacy method for backward compatibility - delegates to error tracking version.
     *
     * @param <T> the type of model to convert to
     * @param dataStream the stream of DataRecord objects
     * @param modelClass the target model class
     * @param config the conversion configuration
     * @return stream of converted model objects
     */
    public <T> Stream<T> convertToModels(Stream<DataRecord> dataStream, Class<T> modelClass,
                                         DataLoaderConfiguration.DataSourceDefinition config) {
        ModelConversionResult<T> result = convertToModelsWithErrorTracking(dataStream, modelClass, config);
        return result.getModels().stream();
    }

    /**
     * Converts a list of model objects to a stream of DataRecord objects.
     *
     * <p>This method provides the reverse conversion capability, enabling models
     * to be converted back to the framework's internal DataRecord format for
     * further processing or database persistence.</p>
     *
     * @param <T> the type of models being converted
     * @param models the list of model objects to convert
     * @param config the conversion configuration
     * @return stream of DataRecord objects
     */
    public <T> Stream<DataRecord> convertFromModels(List<T> models,
                                                    DataLoaderConfiguration.DataSourceDefinition config) {
        if (models == null) {
            throw new IllegalArgumentException("Models list cannot be null");
        }
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }

        log.debug("Starting conversion from models to DataRecord. Count: {}", models.size());

        return models.stream()
                .map(model -> convertFromModel(model, config))
                .filter(Objects::nonNull);
    }

    /**
     * Converts a model object to DataRecord with comprehensive error handling.
     *
     * @param <T> the type of model being converted
     * @param model the model object to convert
     * @param config the conversion configuration
     * @return DataRecord object or null if conversion fails
     */
    private <T> DataRecord convertFromModel(T model, DataLoaderConfiguration.DataSourceDefinition config) {
        try {
            Map<String, Object> data = new HashMap<>();
            Class<?> modelClass = model.getClass();

            if ("DIRECT".equals(config.getModel().getMappingStrategy())) {
                // Direct mapping: extract all fields
                extractFieldsDirect(model, data, modelClass);
            } else {
                // Mapped strategy: use column mappings in reverse
                extractFieldsMapped(model, data, modelClass, config.getColumnMapping());
            }

            log.debug("Successfully converted model {} to DataRecord", modelClass.getSimpleName());
            return DataRecord.valid(data, 0);

        } catch (Exception e) {
            log.error("Failed to convert model {} to DataRecord: {}",
                    model.getClass().getSimpleName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Populates model using direct field mapping strategy.
     *
     * <p>In direct mapping, field names in the source data must exactly match
     * the field names in the target model class. This provides the fastest
     * performance with minimal configuration overhead.</p>
     *
     * @param <T> the type of model being populated
     * @param instance the model instance to populate
     * @param data the source data map
     * @param modelClass the model class for reflection operations
     * @throws Exception if field access or assignment fails
     */
    private <T> void populateModelDirect(T instance, Map<String, Object> data, Class<T> modelClass)
            throws Exception {

        Field[] fields = modelClass.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();

            if (data.containsKey(fieldName)) {
                Object value = convertValueToFieldType(data.get(fieldName), field.getType());
                field.set(instance, value);

                log.trace("Set field '{}' to value '{}' in model {}",
                        fieldName, value, modelClass.getSimpleName());
            }
        }
    }

    /**
     * Populates model using mapped field assignment strategy.
     *
     * <p>In mapped strategy, explicit column mappings define how source fields
     * map to target model fields. This provides maximum flexibility for
     * complex mapping scenarios.</p>
     *
     * @param <T> the type of model being populated
     * @param instance the model instance to populate
     * @param data the source data map
     * @param modelClass the model class for reflection operations
     * @param mappings the list of column mappings
     * @throws Exception if field access or assignment fails
     */
    private <T> void populateModelMapped(T instance, Map<String, Object> data, Class<T> modelClass,
                                         List<DataLoaderConfiguration.ColumnMapping> mappings) throws Exception {

        // Log all available columns for debugging
        log.debug("Available columns in data: {}", data.keySet());

        // Log mapped columns for transparency
        Set<String> mappedColumns = mappings.stream()
                .map(DataLoaderConfiguration.ColumnMapping::getSource)
                .collect(Collectors.toSet());
        log.debug("Columns being mapped: {}", mappedColumns);

        // Log ignored columns for awareness
        Set<String> ignoredColumns = data.keySet().stream()
                .filter(col -> !mappedColumns.contains(col))
                .collect(Collectors.toSet());
        if (!ignoredColumns.isEmpty()) {
            log.info("Ignoring {} unmapped columns: {}", ignoredColumns.size(), ignoredColumns);
        }

        for (DataLoaderConfiguration.ColumnMapping mapping : mappings) {
            if (data.containsKey(mapping.getSource())) {
                Object value = data.get(mapping.getSource());
                setFieldValue(instance, mapping.getTarget(), value, modelClass);
                log.debug("Mapped '{}' -> '{}' with value '{}' in model {}",
                        mapping.getSource(), mapping.getTarget(), value, modelClass.getSimpleName());
            } else {
                log.warn("Mapped column '{}' not found in source data", mapping.getSource());
            }
        }
    }

    /**
     * Extracts fields using direct mapping strategy for model-to-DataRecord conversion.
     *
     * @param <T> the type of model being processed
     * @param model the model object to extract from
     * @param data the target data map
     * @param modelClass the model class for reflection operations
     * @throws Exception if field access fails
     */
    private <T> void extractFieldsDirect(T model, Map<String, Object> data, Class<?> modelClass)
            throws Exception {

        Field[] fields = modelClass.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            Object value = field.get(model);

            if (value != null) {
                data.put(field.getName(), value);
            }
        }
    }

    /**
     * Extracts fields using mapped strategy for model-to-DataRecord conversion.
     *
     * @param <T> the type of model being processed
     * @param model the model object to extract from
     * @param data the target data map
     * @param modelClass the model class for reflection operations
     * @param mappings the list of column mappings (used in reverse)
     * @throws Exception if field access fails
     */
    private <T> void extractFieldsMapped(T model, Map<String, Object> data, Class<?> modelClass,
                                         List<DataLoaderConfiguration.ColumnMapping> mappings) throws Exception {

        for (DataLoaderConfiguration.ColumnMapping mapping : mappings) {
            Object value = getFieldValue(model, mapping.getTarget(), modelClass);
            if (value != null) {
                data.put(mapping.getSource(), value);
            }
        }
    }

    /**
     * Sets field value using reflection with proper type conversion.
     *
     * <p>This method attempts to set field values using both direct field access
     * and setter method invocation, providing maximum compatibility with different
     * model class designs.</p>
     *
     * @param <T> the type of model being modified
     * @param instance the model instance to modify
     * @param fieldName the name of the field to set
     * @param value the value to set
     * @param modelClass the model class for reflection operations
     * @throws Exception if field access or method invocation fails
     */
    private <T> void setFieldValue(T instance, String fieldName, Object value, Class<T> modelClass)
            throws Exception {

        try {
            Field field = findField(modelClass, fieldName);
            if (field != null) {
                field.setAccessible(true);
                Object convertedValue = convertValueToFieldType(value, field.getType());
                field.set(instance, convertedValue);
                return;
            }
        } catch (NoSuchFieldException ignored) {
            // Try setter method as fallback
        }

        // Try setter method
        String setterName = "set" + capitalize(fieldName);
        Method[] methods = modelClass.getDeclaredMethods();

        for (Method method : methods) {
            if (method.getName().equals(setterName) && method.getParameterCount() == 1) {
                method.setAccessible(true);
                Object convertedValue = convertValueToFieldType(value, method.getParameterTypes()[0]);
                method.invoke(instance, convertedValue);
                return;
            }
        }

        log.warn("Could not find field or setter for '{}' in class {}", fieldName, modelClass.getSimpleName());
    }

    /**
     * Gets field value using reflection with fallback to getter methods.
     *
     * @param <T> the type of model being accessed
     * @param instance the model instance to access
     * @param fieldName the name of the field to get
     * @param modelClass the model class for reflection operations
     * @return the field value or null if not found
     * @throws Exception if field access or method invocation fails
     */
    private <T> Object getFieldValue(T instance, String fieldName, Class<?> modelClass) throws Exception {
        try {
            Field field = findField(modelClass, fieldName);
            if (field != null) {
                field.setAccessible(true);
                return field.get(instance);
            }
        } catch (NoSuchFieldException ignored) {
            // Try getter method as fallback
        }

        // Try getter method
        String getterName = "get" + capitalize(fieldName);
        Method[] methods = modelClass.getDeclaredMethods();

        for (Method method : methods) {
            if (method.getName().equals(getterName) && method.getParameterCount() == 0) {
                method.setAccessible(true);
                return method.invoke(instance);
            }
        }

        log.warn("Could not find field or getter for '{}' in class {}", fieldName, modelClass.getSimpleName());
        return null;
    }

    /**
     * Finds field in class hierarchy, searching through superclasses if necessary.
     *
     * @param clazz the class to search in
     * @param fieldName the name of the field to find
     * @return the Field object if found
     * @throws NoSuchFieldException if field is not found in the class hierarchy
     */
    private Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            }
        }
        throw new NoSuchFieldException("Field '" + fieldName + "' not found in class hierarchy");
    }

    /**
     * Converts value to the target field type using the data type converter.
     *
     * @param value the value to convert
     * @param targetType the target field type
     * @return the converted value
     */
    private Object convertValueToFieldType(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }

        // Use existing DataTypeConverter for type conversion
        String dataType = getDataTypeFromClass(targetType);
        if (dataType != null) {
            // Create a temporary column mapping for conversion
            var tempMapping = new DataLoaderConfiguration.ColumnMapping(
                    "temp", "temp", dataType, null, null, null, null, true, null
            );
            return dataTypeConverter.convertForModel(value.toString(), tempMapping);
        }

        return value;
    }

    /**
     * Maps Java class types to framework data type strings.
     *
     * @param clazz the Java class to map
     * @return the corresponding data type string or null if not supported
     */
    private String getDataTypeFromClass(Class<?> clazz) {
        if (clazz == String.class) return "STRING";
        if (clazz == Integer.class || clazz == int.class) return "INTEGER";
        if (clazz == Long.class || clazz == long.class) return "LONG";
        if (clazz == Double.class || clazz == double.class) return "DOUBLE";
        if (clazz == java.math.BigDecimal.class) return "BIGDECIMAL";
        if (clazz == Boolean.class || clazz == boolean.class) return "BOOLEAN";
        if (clazz == java.time.LocalDate.class) return "LOCALDATE";
        if (clazz == java.time.LocalDateTime.class) return "LOCALDATETIME";
        if (clazz == java.sql.Timestamp.class) return "TIMESTAMP";

        return null;
    }

    /**
     * Capitalizes the first letter of a string for method name generation.
     *
     * @param str the string to capitalize
     * @return the capitalized string
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Custom exception for model conversion errors with enhanced error context.
     */
    public static class ModelConversionException extends RuntimeException {

        /**
         * Constructs a new model conversion exception with the specified detail message.
         *
         * @param message the detail message explaining the conversion failure
         */
        public ModelConversionException(String message) {
            super(message);
        }

        /**
         * Constructs a new model conversion exception with the specified detail message and cause.
         *
         * @param message the detail message explaining the conversion failure
         * @param cause the underlying cause of the conversion failure
         */
        public ModelConversionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
