package com.java.lld.oops.configdriven.dataloading.loader;

import com.java.lld.oops.configdriven.dataloading.config.DataLoaderConfiguration;
import com.java.lld.oops.configdriven.dataloading.model.DataRecord;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Converts between DataRecord and Model objects using reflection
 * Compatible with Java 11, 17, and 21
 */
@Slf4j
@Component
public class ModelConverter {
    
    private final DataTypeConverter dataTypeConverter;
    private final Validator validator;

    public ModelConverter(DataTypeConverter dataTypeConverter, Validator validator) {
        this.dataTypeConverter = dataTypeConverter;
        this.validator = validator;
    }

    /**
     * Result wrapper for model conversion with error tracking
     */
    @Getter
    public static class ModelConversionResult<T> {
        // Getters
        private final List<T> models;
        private final List<String> errors;
        private final int totalRecords;
        private final int successfulRecords;
        private final int errorRecords;

        public ModelConversionResult(List<T> models, List<String> errors, int totalRecords) {
            this.models = models;
            this.errors = errors;
            this.totalRecords = totalRecords;
            this.successfulRecords = models.size();
            this.errorRecords = errors.size();
        }

    }

    /**
     * Enhanced method that converts DataRecord stream to models with comprehensive error tracking
     */
    public <T> ModelConversionResult<T> convertToModelsWithErrorTracking(Stream<DataRecord> dataStream,
                                                                         Class<T> modelClass,
                                                                         DataLoaderConfiguration.DataSourceDefinition config) {
        log.debug("Starting conversion to model type: {} with error tracking", modelClass.getSimpleName());

        List<T> models = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        AtomicInteger totalRecords = new AtomicInteger(0);

        dataStream.forEach(record -> {
            totalRecords.incrementAndGet();

            if (!record.valid()) {
                String errorMsg = String.format("Record %d is invalid: %s",
                        record.rowNumber(), record.errorMessage());
                errors.add(errorMsg);
                log.warn(errorMsg);
                return;
            }

            try {
                T model = convertToModelSafely(record, modelClass, config);
                if (model != null) {
                    models.add(model);
                    log.debug("Successfully converted record {} to model {}",
                            record.rowNumber(), modelClass.getSimpleName());
                }
            } catch (ModelConversionException e) {
                errors.add(e.getMessage());
                log.error("Conversion failed for record {}: {}", record.rowNumber(), e.getMessage());
            } catch (Exception e) {
                String errorMsg = String.format("Unexpected error converting record %d: %s",
                        record.rowNumber(), e.getMessage());
                errors.add(errorMsg);
                log.error(errorMsg, e);
            }
        });

        log.info("Model conversion completed. Total: {}, Successful: {}, Errors: {}",
                totalRecords.get(), models.size(), errors.size());

        return new ModelConversionResult<>(models, errors, totalRecords.get());
    }

    /**
     * Safe conversion method that handles errors gracefully
     */
    private <T> T convertToModelSafely(DataRecord record, Class<T> modelClass,
                                       DataLoaderConfiguration.DataSourceDefinition config) {
        try {
            T instance = modelClass.getDeclaredConstructor().newInstance();

            if ("DIRECT".equals(config.model().mappingStrategy())) {
                populateModelDirect(instance, record.data(), modelClass);
            } else {
                populateModelMapped(instance, record.data(), modelClass, config.columnMapping());
            }

            // Validate the populated model
            if (config.validation() != null && config.validation().dataQualityChecks()) {
                Set<ConstraintViolation<T>> violations = validator.validate(instance);
                if (!violations.isEmpty()) {
                    StringBuilder errorMessage = new StringBuilder("Validation errors for record " + record.rowNumber() + ": ");
                    violations.forEach(violation ->
                            errorMessage.append(violation.getPropertyPath()).append(" - ").append(violation.getMessage()).append("; "));

                    log.warn("Validation failed for record {}: {}", record.rowNumber(), errorMessage);

                    // Always throw exception to be caught and handled properly
                    throw new ModelConversionException(errorMessage.toString());
                }
            }

            return instance;

        } catch (ModelConversionException e) {
            // Re-throw validation exceptions
            throw e;
        } catch (Exception e) {
            // Wrap other exceptions
            throw new ModelConversionException(
                    String.format("Failed to convert record %d: %s", record.rowNumber(), e.getMessage()), e);
        }
    }

    /**
     * Legacy method for backward compatibility - now delegates to error tracking version
     */
    public <T> Stream<T> convertToModels(Stream<DataRecord> dataStream, Class<T> modelClass,
                                         DataLoaderConfiguration.DataSourceDefinition config) {
        ModelConversionResult<T> result = convertToModelsWithErrorTracking(dataStream, modelClass, config);
        return result.getModels().stream();
    }

    /**
     * Converts a list of model objects to a stream of DataRecord
     */
    public <T> Stream<DataRecord> convertFromModels(List<T> models, DataLoaderConfiguration.DataSourceDefinition config) {
        log.debug("Starting conversion from models to DataRecord. Count: {}", models.size());

        return models.stream()
                .map(model -> convertFromModel(model, config))
                .filter(java.util.Objects::nonNull);
    }

    /**
     * Converts a single DataRecord to a model object with validation
     */
    private <T> T convertToModel(DataRecord record, Class<T> modelClass, DataLoaderConfiguration.DataSourceDefinition config) {
        try {
            T instance = modelClass.getDeclaredConstructor().newInstance();

            if ("DIRECT".equals(config.model().mappingStrategy())) {
                populateModelDirect(instance, record.data(), modelClass);
            } else {
                populateModelMapped(instance, record.data(), modelClass, config.columnMapping());
            }

            // Validate the populated model
            if (config.validation() != null && config.validation().dataQualityChecks()) {
                Set<ConstraintViolation<T>> violations = validator.validate(instance);
                if (!violations.isEmpty()) {
                    StringBuilder errorMessage = new StringBuilder("Validation errors for record " + record.rowNumber() + ": ");
                    violations.forEach(violation ->
                            errorMessage.append(violation.getPropertyPath()).append(" - ").append(violation.getMessage()).append("; "));

                    log.error("Validation failed for record {}: {}", record.rowNumber(), errorMessage);

                    if (config.model().strictMapping()) {
                        throw new ModelConversionException(errorMessage.toString());
                    }
                    return null; // Skip invalid records in lenient mode
                }
            }

            log.debug("Successfully converted record {} to model {}",
                    record.rowNumber(), modelClass.getSimpleName());
            return instance;

        } catch (Exception e) {
            log.error("Failed to convert record {} to model {}: {}",
                    record.rowNumber(), modelClass.getSimpleName(), e.getMessage(), e);

            if (config.model().strictMapping()) {
                throw new ModelConversionException(
                        String.format("Strict mapping failed for record %d: %s",
                                record.rowNumber(), e.getMessage()), e);
            }
            return null;
        }
    }


    /**
     * Converts a model object to DataRecord
     */
    private <T> DataRecord convertFromModel(T model, DataLoaderConfiguration.DataSourceDefinition config) {
        try {
            Map<String, Object> data = new HashMap<>();
            Class<?> modelClass = model.getClass();

            if ("DIRECT".equals(config.model().mappingStrategy())) {
                // Direct mapping: extract all fields
                extractFieldsDirect(model, data, modelClass);
            } else {
                // Mapped strategy: use column mappings in reverse
                extractFieldsMapped(model, data, modelClass, config.columnMapping());
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
     * Populates model using direct field mapping
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
     * Populates model using column mappings
     */
    private <T> void populateModelMapped(T instance, Map<String, Object> data, Class<T> modelClass,
                                         List<DataLoaderConfiguration.ColumnMapping> mappings) throws Exception {

        for (DataLoaderConfiguration.ColumnMapping mapping : mappings) {
            if (data.containsKey(mapping.source())) {
                Object value = data.get(mapping.source());
                setFieldValue(instance, mapping.target(), value, modelClass);

                log.debug("Mapped '{}' -> '{}' with value '{}' in model {}",
                        mapping.source(), mapping.target(), value, modelClass.getSimpleName());
            }
        }
    }

    /**
     * Extracts fields using direct mapping
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
     * Extracts fields using column mappings (reverse)
     */
    private <T> void extractFieldsMapped(T model, Map<String, Object> data, Class<?> modelClass,
                                         List<DataLoaderConfiguration.ColumnMapping> mappings) throws Exception {

        for (DataLoaderConfiguration.ColumnMapping mapping : mappings) {
            Object value = getFieldValue(model, mapping.target(), modelClass);
            if (value != null) {
                data.put(mapping.source(), value);
            }
        }
    }

    /**
     * Sets field value using reflection with proper type conversion
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
            // Try setter method
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
     * Gets field value using reflection
     */
    private <T> Object getFieldValue(T instance, String fieldName, Class<?> modelClass) throws Exception {
        try {
            Field field = findField(modelClass, fieldName);
            if (field != null) {
                field.setAccessible(true);
                return field.get(instance);
            }
        } catch (NoSuchFieldException ignored) {
            // Try getter method
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
     * Finds field in class hierarchy
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
     * Converts value to the target field type
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
                    "temp", "temp", dataType, null, null, null, null, "yes", null
            );
            return dataTypeConverter.convertForModel(value.toString(), tempMapping);
        }

        return value;
    }

    /**
     * Maps Java class types to our data type strings
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
     * Capitalizes the first letter of a string
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Custom exception for model conversion errors
     */
    public static class ModelConversionException extends RuntimeException {
        public ModelConversionException(String message) {
            super(message);
        }

        public ModelConversionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}