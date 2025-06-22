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
 * Service responsible for processing data records from data sources.
 * It applies transformations like column mapping (with type conversion) and validation based on config.
 * Enhancements include:
 * <ul>
 *     <li>Column value conversion using {@link DataTypeConverter}</li>
 *     <li>Graceful fallback using default values when source is null</li>
 *     <li>Detailed logging and error tracking</li>
 * </ul>
 *
 * @author sathwick
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataProcessor {

    private final DataTypeConverter dataTypeConverter;

    /**
     * Processes the incoming data stream by:
     * <ul>
     *     <li>Filtering valid records</li>
     *     <li>Applying column mapping with type conversion</li>
     *     <li>Validating required fields</li>
     * </ul>
     *
     * @param dataStream The input stream of {@link DataRecord}
     * @param config     The configuration definition for the data source
     * @return A processed and validated stream of {@link DataRecord}
     */
    public Stream<DataRecord> processData(Stream<DataRecord> dataStream, DataLoaderConfiguration.DataSourceDefinition config) {
        log.info("Starting data processing for source: {}", config.identifier().strip());

        return dataStream
                .filter(DataRecord::valid)
                .map(record -> applyColumnMapping(record, config.columnMapping()))
                .map(record -> validateRecord(record, config));
    }

    /**
     * Applies column mapping with optional type conversion based on configuration.
     *
     * @param record   The original data record
     * @param mappings List of column mappings from source to target
     * @return A new {@link DataRecord} with mapped and type-converted fields, or marked invalid if conversion fails
     */
    private DataRecord applyColumnMapping(DataRecord record, List<DataLoaderConfiguration.ColumnMapping> mappings) {
        if (mappings == null || mappings.isEmpty()) {
            log.debug("No column mappings configured for row {}", record.rowNumber());
            return record;
        }

        Map<String, Object> mappedData = new HashMap<>();

        for (DataLoaderConfiguration.ColumnMapping mapping : mappings) {
            String sourceKey = mapping.source().strip();
            String targetKey = mapping.target().strip();
            Object sourceValue = record.data().get(sourceKey);

            try {
                Object convertedValue;

                if (sourceValue != null) {
                    convertedValue = dataTypeConverter.convertForDatabase(sourceValue.toString(), mapping);
                    log.debug("Row {}: Mapped '{}' → '{}' (converted: {} → {}, type: {})",
                            record.rowNumber(), sourceKey, targetKey,
                            sourceValue, convertedValue, mapping.dataType());
                } else {
                    log.debug("Row {}: Source key '{}' missing or null, attempting default for '{}'",
                            record.rowNumber(), sourceKey, targetKey);
                    convertedValue = dataTypeConverter.convertForDatabase(null, mapping);
                }
                if (convertedValue != null) {
                    mappedData.put(targetKey, convertedValue);
                }
            } catch (DataTypeConverter.DataConversionException e) {
                String errorMsg = String.format("Data conversion error for column '%s': %s", sourceKey, e.getMessage());
                log.warn("Row {} marked invalid - {}", record.rowNumber(), errorMsg, e);
                return DataRecord.invalid(record.data(), record.rowNumber(), errorMsg);
            } catch (Exception e) {
                log.error("Unexpected error in mapping for row {}: {}", record.rowNumber(), e.getMessage(), e);
                return DataRecord.invalid(record.data(), record.rowNumber(), "Unexpected mapping error");
            }
        }

        return new DataRecord(mappedData, record.rowNumber(), record.valid(), record.errorMessage());
    }

    /**
     * Validates the data record by checking for required fields based on the provided configuration.
     *
     * @param record The record to validate
     * @param config The data source configuration
     * @return A valid or invalid {@link DataRecord} based on required fields
     */
    private DataRecord validateRecord(DataRecord record, DataLoaderConfiguration.DataSourceDefinition config) {
        if (config.validation() == null || !config.validation().dataQualityChecks()) {
            return record;
        }

        var requiredColumns = config.validation().requiredColumns();
        if (requiredColumns != null) {
            for (String column : requiredColumns) {
                String key = column.strip();
                Object value = record.data().get(key);
                if (value == null || (value instanceof String && ((String) value).strip().isEmpty())) {
                    String error = "Missing required column: " + key;
                    log.warn("Row {} marked invalid - {}", record.rowNumber(), error);
                    return DataRecord.invalid(record.data(), record.rowNumber(), error);
                }
            }
        }

        return record;
    }
}