package com.java.lld.oops.configdriven.dataloading.service;

import com.java.lld.oops.configdriven.dataloading.config.DataLoaderConfiguration;
import com.java.lld.oops.configdriven.dataloading.loader.DataTypeConverter;
import com.java.lld.oops.configdriven.dataloading.model.DataRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Service responsible for processing data records from various data sources with comprehensive
 * transformation and validation capabilities.
 *
 * <p>This processor serves as the central transformation engine in the data loading pipeline,
 * handling column mapping with type conversion, data validation, and error handling. It ensures
 * data quality and consistency before records are persisted to the database.</p>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *     <li><b>Column Mapping:</b> Maps source columns to target columns with configurable transformations</li>
 *     <li><b>Type Conversion:</b> Converts data types using {@link DataTypeConverter} with proper error handling</li>
 *     <li><b>Data Validation:</b> Validates required fields and data quality based on configuration</li>
 *     <li><b>Default Value Handling:</b> Gracefully handles missing values with configurable defaults</li>
 *     <li><b>Error Tracking:</b> Comprehensive logging and error tracking for debugging and monitoring</li>
 * </ul>
 *
 * <p><b>Processing Pipeline:</b></p>
 * <ol>
 *     <li>Filter valid records from the input stream</li>
 *     <li>Apply column mapping with type conversion</li>
 *     <li>Validate required fields and data quality constraints</li>
 *     <li>Return processed stream with valid/invalid record markers</li>
 * </ol>
 *
 * <p><b>Error Handling Strategy:</b></p>
 * <ul>
 *     <li>Records with conversion errors are marked as invalid but remain in the stream</li>
 *     <li>Detailed error messages are attached to invalid records for debugging</li>
 *     <li>Processing continues for other records even when individual records fail</li>
 * </ul>
 *
 * <p><b>Java 11 Compatibility:</b></p>
 * <ul>
 *     <li>Uses traditional getter methods instead of record accessors</li>
 *     <li>Compatible with Java 11, 17, and 21</li>
 *     <li>No behavioral changes from higher Java versions</li>
 * </ul>
 *
 * @author sathwick
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataProcessor {

    private final DataTypeConverter dataTypeConverter;

    /**
     * Processes the incoming data stream by applying transformations and validations.
     *
     * <p>This method orchestrates the complete data processing pipeline:</p>
     * <ol>
     *     <li><b>Filtering:</b> Removes already invalid records from processing</li>
     *     <li><b>Column Mapping:</b> Applies source-to-target column mappings with type conversion</li>
     *     <li><b>Validation:</b> Validates required fields and data quality constraints</li>
     * </ol>
     *
     * <p><b>Stream Processing Benefits:</b></p>
     * <ul>
     *     <li>Memory efficient - processes records one at a time</li>
     *     <li>Lazy evaluation - transformations applied only when needed</li>
     *     <li>Parallel processing capable for large datasets</li>
     * </ul>
     *
     * @param dataStream The input stream of {@link DataRecord} objects to process
     * @param config     The configuration definition containing mapping and validation rules
     * @return A processed and validated stream of {@link DataRecord} objects
     * @throws IllegalArgumentException if config is null or contains invalid configuration
     */
    public Stream<DataRecord> processData(Stream<DataRecord> dataStream,
                                          DataLoaderConfiguration.DataSourceDefinition config) {
        if (dataStream == null) {
            throw new IllegalArgumentException("Data stream cannot be null");
        }
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }

        log.debug("Starting data processing with configuration: {}", config.getType());

        return dataStream
                .filter(DataRecord::isValid)  // Java 11 compatible getter method
                .map(record -> applyColumnMapping(record, config.getColumnMapping()))
                .map(record -> validateRecord(record, config));
    }

    /**
     * Applies column mapping with optional type conversion based on configuration.
     *
     * <p>This method handles the core transformation logic:</p>
     * <ul>
     *     <li><b>Source Mapping:</b> Maps source column names to target column names</li>
     *     <li><b>Type Conversion:</b> Converts values to target data types</li>
     *     <li><b>Default Handling:</b> Applies default values for missing source data</li>
     *     <li><b>Error Recovery:</b> Gracefully handles conversion errors</li>
     * </ul>
     *
     * <p><b>Conversion Process:</b></p>
     * <ol>
     *     <li>Extract source value using source column name</li>
     *     <li>Apply type conversion using {@link DataTypeConverter}</li>
     *     <li>Store converted value using target column name</li>
     *     <li>Handle missing values with defaults if configured</li>
     * </ol>
     *
     * <p><b>Error Handling:</b></p>
     * <ul>
     *     <li>Conversion errors result in invalid records with detailed error messages</li>
     *     <li>Missing source columns trigger default value logic</li>
     *     <li>Unexpected errors are logged and result in invalid records</li>
     * </ul>
     *
     * @param record   The original data record containing source data
     * @param mappings List of column mappings defining source-to-target transformations
     * @return A new {@link DataRecord} with mapped and type-converted fields,
     *         or marked invalid if conversion fails
     */
    private DataRecord applyColumnMapping(DataRecord record,
                                          List<DataLoaderConfiguration.ColumnMapping> mappings) {
        if (mappings == null || mappings.isEmpty()) {
            log.debug("No column mappings configured for row {}, returning original record",
                    record.getRowNumber());
            return record;
        }

        log.trace("Applying {} column mappings to row {}", mappings.size(), record.getRowNumber());
        Map<String, Object> mappedData = new HashMap<>();

        for (DataLoaderConfiguration.ColumnMapping mapping : mappings) {
            String sourceKey = mapping.getSource().strip();
            String targetKey = mapping.getTarget().strip();
            Object sourceValue = record.getData().get(sourceKey);

            try {
                Object convertedValue = processColumnValue(sourceValue, mapping, record.getRowNumber(),
                        sourceKey, targetKey);

                if (convertedValue != null) {
                    mappedData.put(targetKey, convertedValue);
                    log.trace("Row {}: Successfully mapped '{}' -> '{}' with value: {}",
                            record.getRowNumber(), sourceKey, targetKey, convertedValue);
                }

            } catch (DataTypeConverter.DataConversionException e) {
                String errorMsg = String.format("Data conversion error for column '%s': %s",
                        sourceKey, e.getMessage());
                log.warn("Row {} marked invalid - {}", record.getRowNumber(), errorMsg);
                return DataRecord.invalid(record.getData(), record.getRowNumber(), errorMsg);

            } catch (Exception e) {
                log.error("Unexpected error in mapping for row {}, column '{}': {}",
                        record.getRowNumber(), sourceKey, e.getMessage(), e);
                return DataRecord.invalid(record.getData(), record.getRowNumber(),
                        "Unexpected mapping error for column: " + sourceKey);
            }
        }

        return new DataRecord(mappedData, record.getRowNumber(), record.isValid(), record.getErrorMessage());
    }

    /**
     * Processes a single column value with type conversion and default value handling.
     *
     * @param sourceValue The original value from the source data
     * @param mapping     The column mapping configuration
     * @param rowNumber   The row number for logging purposes
     * @param sourceKey   The source column name for logging
     * @param targetKey   The target column name for logging
     * @return The converted value ready for database insertion
     * @throws DataTypeConverter.DataConversionException if conversion fails
     */
    private Object processColumnValue(Object sourceValue, DataLoaderConfiguration.ColumnMapping mapping,
                                      int rowNumber, String sourceKey, String targetKey) {
        Object convertedValue;

        if (sourceValue != null) {
            convertedValue = dataTypeConverter.convertForDatabase(sourceValue.toString(), mapping);
            log.debug("Row {}: Mapped '{}' → '{}' (converted: {} → {}, type: {})",
                    rowNumber, sourceKey, targetKey, sourceValue, convertedValue, mapping.getDataType());
        } else {
            log.debug("Row {}: Source key '{}' missing or null, attempting default for '{}'",
                    rowNumber, sourceKey, targetKey);
            convertedValue = dataTypeConverter.convertForDatabase(null, mapping);
        }

        return convertedValue;
    }

    /**
     * Validates the data record by checking for required fields and data quality constraints.
     *
     * <p>This method performs comprehensive validation based on configuration:</p>
     * <ul>
     *     <li><b>Required Field Validation:</b> Ensures all required columns are present and non-empty</li>
     *     <li><b>Data Quality Checks:</b> Applies additional quality constraints if enabled</li>
     *     <li><b>Null Value Handling:</b> Validates that required fields contain meaningful data</li>
     *     <li><b>Empty String Detection:</b> Treats empty/whitespace strings as missing values</li>
     * </ul>
     *
     * <p><b>Validation Rules:</b></p>
     * <ol>
     *     <li>Skip validation if data quality checks are disabled</li>
     *     <li>Check each required column for presence and non-null values</li>
     *     <li>Validate that string values are not empty or whitespace-only</li>
     *     <li>Mark record as invalid if any required field fails validation</li>
     * </ol>
     *
     * <p><b>Performance Considerations:</b></p>
     * <ul>
     *     <li>Validation is skipped entirely if not configured, improving performance</li>
     *     <li>Early exit on first validation failure for efficiency</li>
     *     <li>Minimal object creation during validation process</li>
     * </ul>
     *
     * @param record The record to validate
     * @param config The data source configuration containing validation rules
     * @return A valid or invalid {@link DataRecord} based on validation results
     */
    private DataRecord validateRecord(DataRecord record,
                                      DataLoaderConfiguration.DataSourceDefinition config) {
        // Skip validation if not configured
        if (config.getValidation() == null || !config.getValidation().isDataQualityChecks()) {
            log.trace("Row {}: Validation skipped (not configured)", record.getRowNumber());
            return record;
        }

        List<String> requiredColumns = config.getValidation().getRequiredColumns();
        if (requiredColumns == null || requiredColumns.isEmpty()) {
            log.trace("Row {}: No required columns configured", record.getRowNumber());
            return record;
        }

        log.trace("Row {}: Validating {} required columns", record.getRowNumber(), requiredColumns.size());

        for (String column : requiredColumns) {
            String key = column.strip();
            Object value = record.getData().get(key);

            if (isValueMissing(value)) {
                String error = "Missing required column: " + key;
                log.warn("Row {} marked invalid - {}", record.getRowNumber(), error);
                return DataRecord.invalid(record.getData(), record.getRowNumber(), error);
            }
        }

        log.trace("Row {}: All required columns validated successfully", record.getRowNumber());
        return record;
    }

    /**
     * Determines if a value is considered missing for validation purposes.
     *
     * <p>A value is considered missing if:</p>
     * <ul>
     *     <li>It is null</li>
     *     <li>It is an empty string</li>
     *     <li>It is a string containing only whitespace</li>
     * </ul>
     *
     * @param value The value to check
     * @return true if the value is missing, false otherwise
     */
    private boolean isValueMissing(Object value) {
        return value == null || (value instanceof String && ((String) value).strip().isEmpty());
    }
}
