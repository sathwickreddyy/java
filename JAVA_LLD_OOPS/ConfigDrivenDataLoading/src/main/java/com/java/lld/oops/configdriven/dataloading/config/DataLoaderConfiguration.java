package com.java.lld.oops.configdriven.dataloading.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

/**
 * Configuration properties for data loading with enhanced type support
 * @param dataSources data-sources from data-sources.yaml
 * @author sathwick
 */
@Validated
@ConfigurationProperties(prefix = "data-sourcing")
public record DataLoaderConfiguration(
        @NotNull
        @NotEmpty
        @Valid
        Map<String, DataSourceDefinition> dataSources
) {

    public record DataSourceDefinition(
            @NotBlank(message = "Type cannot be blank")
            @Pattern(regexp = "^(CSV|EXCEL|JSON|API|DATABASE)$",
                    message = "Invalid input feed type. Must be one of: CSV, EXCEL, JSON, API, DATABASE")
            String type,

            @NotNull(message = "Source configuration cannot be null")
            @Valid
            SourceConfig source,

            @Valid
            TargetConfig target,

            @NotNull(message = "Column mapping cannot be null")
            @NotEmpty(message = "Column mapping cannot be empty")
            @Valid
            List<ColumnMapping> columnMapping,

            ValidationConfig  validation,

            // Model-specific configuration
            ModelConfig model
    ) {}

    public record SourceConfig(
            String filePath,
            String delimiter,
            Boolean header,
            String encoding,
            String sheetName,
            @Min(value = 0, message = "Skip rows must be non-negative")
            Integer skipRows,
            String url,
            String method,
            Map<String, String> headers,
            @Min(value = 1000, message = "Timeout must be at least 1000ms")
            Integer timeout,
            @Min(value = 0, message = "Retry attempts must be non-negative")
            @Max(value = 5, message = "Retry attempts cannot exceed 5")
            Integer retryAttempts,
            String jsonPath
    ) {}

    public record TargetConfig(
            String table,
            @Pattern(regexp = "^(table|model)$",
                    message = "Target type must be either 'table' or 'model'")
            String type,
            @Min(value = 1, message = "Batch size must be at least 1")
            @Max(value = 10000, message = "Batch size cannot exceed 10000")
            Integer batchSize,
            Boolean isBiTemporal
    ) {}

    public record ColumnMapping(
            @NotNull(message = "Data type cannot be null")
            @NotBlank(message = "Source column cannot be blank")
            String source,

            @NotNull(message = "Data type cannot be null")
            @NotBlank(message = "Target column cannot be blank")
            String target,

            @NotBlank(message = "Data type cannot be blank")
            @Pattern(regexp = "^(STRING|INTEGER|LONG|DOUBLE|BIGDECIMAL|BOOLEAN|LOCALDATE|LOCALDATETIME|TIMESTAMP)$",
                    message = "Invalid data type. Must be one of: STRING, INTEGER, LONG, DOUBLE, BIGDECIMAL, BOOLEAN, LOCALDATE, LOCALDATETIME, TIMESTAMP")
            String dataType,

            // Date/Time formatting options
            String sourceDateFormat,
            String targetDateFormat,
            String timeZone,

            // Numeric formatting options
            String decimalFormat,

            // Validation options / Data type conversion required or not
            @Pattern(regexp = "^(yes|no|1|0|y|n|YES|NO|Y|N|Yes|No)$", message = "Invalid Input must be one of: yes/no/1/0/y/n/YES/NO/Y/N/Yes/No")
            String dataTypeValidationRequired,
            String defaultValue
    ) {
        // Default constructor with sensible defaults
        public ColumnMapping {
            if (dataType == null) {
                dataType = "STRING";
            }
            if (dataTypeValidationRequired == null) {
                dataTypeValidationRequired = "yes";
            }
        }
    }

    /**
     * Configuration for mapping data source columns to DTO fields.
     *
     * <p><b>Mapping Strategies:</b></p>
     *
     * <ul>
     *   <li><b>DIRECT</b>:
     *     <ul>
     *       <li>Source column names must exactly match the DTO field names.</li>
     *       <li>No need to define <code>columnMapping</code> in YAML.</li>
     *       <li>Simple, fast, and requires minimal configuration.</li>
     *     </ul>
     *   </li>
     *
     *   <li><b>MAPPED</b>:
     *     <ul>
     *       <li>Uses the <code>columnMapping</code> section in YAML to map source columns to DTO fields.</li>
     *       <li>Allows renaming and transformation of field names.</li>
     *       <li>Supports complex mapping use cases (e.g., nested fields, different naming conventions).</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * <p><b>YAML Example for MAPPED strategy:</b></p>
     *
     * <pre>
     * mappingStrategy: MAPPED
     * columnMapping:
     *   - source: "currency_pair"   # CSV column name
     *     target: "currency"        # DTO field name
     *   - source: "trade_date"
     *     target: "tradeDate"
     * </pre>
     *
     * <p><b>Strict Mapping Behavior:</b></p>
     *
     * <ul>
     *   <li><b>strictMapping: true</b>
     *     <ul>
     *       <li>Enforces strict field mapping.</li>
     *       <li>Throws an exception if any required field is missing or mapping fails.</li>
     *       <li>Stops processing immediately on the first error.</li>
     *       <li>Recommended for production use to ensure data quality and integrity.</li>
     *     </ul>
     *   </li>
     *
     *   <li><b>strictMapping: false</b>
     *     <ul>
     *       <li>Continues processing even if some fields fail to map.</li>
     *       <li>Logs a warning for each failed mapping.</li>
     *       <li>Skips problematic records but still processes valid ones.</li>
     *       <li>Useful for debugging, partial loads, or exploratory data analysis.</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * <p><b>Example Error Handling:</b></p>
     *
     * <pre>{@code
     * // strictMapping: true
     * if (fieldMappingFails) {
     *     throw new ModelConversionException("Required field 'currency' not found");
     * }
     *
     * // strictMapping: false
     * if (fieldMappingFails) {
     *     logger.warn("Field 'currency' mapping failed, skipping record");
     *     return null; // Skip this record
     * }
     * }</pre>
     */
    public record ModelConfig(
            @NotBlank(message = "Model class name cannot be blank when target type is 'model'")
            String className,

            String packageName,


            @Pattern(regexp = "^(DIRECT|MAPPED)$",
                    message = "Mapping strategy must be either 'DIRECT' or 'MAPPED'")
            String mappingStrategy,

            Boolean strictMapping,

            String dateFormat,
            String timeZone
    ) {
        // Default constructor with sensible defaults
        public ModelConfig {
            if (mappingStrategy == null) {
                mappingStrategy = "MAPPED";
            }
            if (strictMapping == null) {
                strictMapping = true;
            }
        }
    }

    public record ValidationConfig(
            List<String> requiredColumns,
            boolean dataQualityChecks
    ) {}
}
