package com.java.lld.oops.configdriven.dataloading.service;

import com.java.lld.oops.configdriven.dataloading.config.DataLoaderConfiguration;
import com.java.lld.oops.configdriven.dataloading.model.DataRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Service responsible for processing data records from data sources.
 * It applies transformations like column mapping from source to destination and validation based on config.
 *
 * @author sathwick
 */
@Slf4j
@Service
public class DataProcessor {

    /**
     * Processes the incoming data stream by:
     * <ul>
     *     <li>Filtering valid records</li>
     *     <li>Applying column mapping</li>
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
     * Applies column mapping to transform record fields according to the configuration.
     *
     * @param record   The original data record
     * @param mappings List of column mappings from source to target
     * @return A new {@link DataRecord} with mapped fields
     */
    private DataRecord applyColumnMapping(DataRecord record, List<DataLoaderConfiguration.ColumnMapping> mappings) {
        if (mappings == null || mappings.isEmpty()) {
            log.debug("No column mappings configured for row {}", record.rowNumber());
            return record;
        }

        Map<String, Object> mappedData = new HashMap<>();

        for (DataLoaderConfiguration.ColumnMapping mapping : mappings) {
            try {
                String sourceKey = mapping.source().strip();
                String targetKey = mapping.target().strip();
                Object value = record.data().get(sourceKey);
                if (value != null) {
                    mappedData.put(targetKey, value);
                } else {
                    log.debug("Source key '{}' not present in row {} during mapping", sourceKey, record.rowNumber());
                }
            } catch (Exception e) {
                log.warn("Error mapping column in row {}: {}", record.rowNumber(), e.getMessage(), e);
            }
        }

        return new DataRecord(mappedData, record.rowNumber(), record.valid(), record.errorMessage());
    }

    /**
     * Validates the data record by checking for required fields.
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