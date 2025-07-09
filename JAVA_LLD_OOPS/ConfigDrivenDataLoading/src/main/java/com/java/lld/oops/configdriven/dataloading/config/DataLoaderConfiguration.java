package com.java.lld.oops.configdriven.dataloading.config;

import javax.validation.Valid;
import javax.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

/**
 * Comprehensive configuration properties for the config-driven data loading framework.
 *
 * <p>This configuration class serves as the central definition for all data loading operations,
 * providing a flexible and extensible way to configure data sources, transformations, and
 * target destinations without requiring code changes.</p>
 *
 * <p><b>Configuration Structure:</b></p>
 * <ul>
 *     <li><b>Data Sources:</b> Map of named data source definitions</li>
 *     <li><b>Source Configuration:</b> Input source details (files, APIs, databases)</li>
 *     <li><b>Target Configuration:</b> Output destination settings</li>
 *     <li><b>Column Mapping:</b> Field transformation and type conversion rules</li>
 *     <li><b>Validation Rules:</b> Data quality and integrity constraints</li>
 *     <li><b>Model Configuration:</b> DTO mapping and conversion settings</li>
 * </ul>
 *
 * <p><b>Supported Data Source Types:</b></p>
 * <ul>
 *     <li><b>CSV:</b> Comma-separated values with configurable delimiters and encoding</li>
 *     <li><b>EXCEL:</b> Microsoft Excel files with sheet and row selection</li>
 *     <li><b>JSON:</b> JSON files and API responses with JSONPath extraction</li>
 *     <li><b>API:</b> REST API endpoints with authentication and retry logic</li>
 *     <li><b>DATABASE:</b> Database queries and stored procedure results</li>
 * </ul>
 *
 * <p><b>Configuration Loading:</b></p>
 * <ul>
 *     <li>Loaded from YAML files using Spring Boot's configuration binding</li>
 *     <li>Supports environment variable substitution</li>
 *     <li>Validates configuration at startup with detailed error messages</li>
 *     <li>Hot-reload capable for development environments</li>
 * </ul>
 *
 * <p><b>Java 11 Compatibility:</b></p>
 * <ul>
 *     <li>Uses traditional classes instead of records for Java 11 support</li>
 *     <li>Lombok annotations provide getter/setter methods</li>
 *     <li>Bean Validation annotations ensure configuration integrity</li>
 *     <li>Compatible with Java 11, 17, and 21</li>
 * </ul>
 *
 * <p><b>Example Configuration:</b></p>
 * <pre>{@code
 * data-sources:
 *   data-sources:
 *     market_data_csv:
 *       type: "CSV"
 *       source:
 *         filePath: "/data/market_data.csv"
 *         delimiter: ","
 *         header: true
 *       target:
 *         schema: "public"
 *         table: "market_trends"
 *         type: "table"
 *       columnMapping:
 *         - source: "date"
 *           target: "trade_date"
 *           dataType: "LOCALDATE"
 * }</pre>
 *
 * @author sathwick
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Validated
@ConfigurationProperties(prefix = "data-sourcing")
public class DataLoaderConfiguration {

    /**
     * Map of data source definitions keyed by their unique names.
     * Each data source represents a complete configuration for loading data
     * from a specific source to a target destination.
     */
    @NotNull(message = "Data sources configuration cannot be null")
    @NotEmpty(message = "At least one data source must be configured")
    @Valid
    private Map<String, DataSourceDefinition> dataSources;

    /**
     * Complete definition of a data source including source, target, mappings, and validation rules.
     *
     * <p>Each data source definition encapsulates all the information needed to:</p>
     * <ul>
     *     <li>Connect to and read from a data source</li>
     *     <li>Transform and map data fields</li>
     *     <li>Validate data quality and integrity</li>
     *     <li>Write data to the target destination</li>
     * </ul>
     *
     * <p><b>Configuration Validation:</b></p>
     * <ul>
     *     <li>All required fields are validated at startup</li>
     *     <li>Data type constraints are enforced</li>
     *     <li>Cross-field validation ensures configuration consistency</li>
     * </ul>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataSourceDefinition {

        /**
         * The type of data source (CSV, EXCEL, JSON, API, DATABASE).
         * This determines which data loader implementation will be used.
         */
        @NotBlank(message = "Data source type cannot be blank")
        @Pattern(regexp = "^(CSV|EXCEL|JSON|API|DATABASE)$",
                message = "Invalid data source type. Must be one of: CSV, EXCEL, JSON, API, DATABASE")
        private String type;

        /**
         * Source configuration containing connection and reading parameters.
         * The specific fields used depend on the data source type.
         */
        @NotNull(message = "Source configuration cannot be null")
        @Valid
        private SourceConfig source;

        /**
         * Target configuration specifying where and how to write the processed data.
         */
        @NotNull(message = "Target configuration cannot be null")
        @Valid
        private TargetConfig target;

        /**
         * List of column mappings defining how source fields map to target fields.
         * Includes type conversion and transformation rules.
         */
        @NotNull(message = "Column mapping configuration cannot be null")
        @NotEmpty(message = "At least one column mapping must be defined")
        @Valid
        private List<ColumnMapping> columnMapping;

        /**
         * Optional validation configuration for data quality checks.
         */
        private ValidationConfig validation;

        /**
         * Optional model configuration for DTO-based loading.
         * Required when target type is "model".
         */
        private ModelConfig model;
    }


    /**
     * Configuration for data source connection and reading parameters.
     *
     * <p>This class contains all the parameters needed to connect to and read from
     * various types of data sources. Not all fields are used by every source type:</p>
     *
     * <ul>
     *     <li><b>File Sources (CSV, Excel, JSON):</b> filePath, encoding, delimiter, etc.</li>
     *     <li><b>API Sources:</b> url, method, headers, timeout, retryAttempts</li>
     *     <li><b>Database Sources:</b> connection parameters and query definitions</li>
     * </ul>
     *
     * <p><b>Security Considerations:</b></p>
     * <ul>
     *     <li>Sensitive information like API keys should use environment variables</li>
     *     <li>File paths should be validated for security</li>
     *     <li>Connection timeouts prevent hanging operations</li>
     * </ul>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceConfig {

        // File-based source parameters
        /**
         * File path for file-based sources (CSV, Excel, JSON).
         * Supports absolute and relative paths.
         */
        private String filePath;

        /**
         * Field delimiter for CSV files (default: comma).
         */
        private String delimiter;

        /**
         * Whether the first row contains headers (CSV/Excel).
         */
        private Boolean header;

        /**
         * Character encoding for file reading (default: UTF-8).
         */
        private String encoding;

        /**
         * Excel sheet name to read from (Excel sources only).
         */
        private String sheetName;

        /**
         * Number of rows to skip at the beginning (Excel sources).
         */
        @Min(value = 0, message = "Skip rows must be non-negative")
        private Integer skipRows;

        // API-based source parameters
        /**
         * URL endpoint for API sources.
         */
        private String url;

        /**
         * HTTP method for API calls (GET, POST, PUT, etc.).
         */
        private String method;

        /**
         * HTTP headers for API requests (including authentication).
         */
        private Map<String, String> headers;

        /**
         * Request timeout in milliseconds for API calls.
         */
        @Min(value = 1000, message = "Timeout must be at least 1000ms")
        private Integer timeout;

        /**
         * Number of retry attempts for failed API calls.
         */
        @Min(value = 0, message = "Retry attempts must be non-negative")
        @Max(value = 5, message = "Retry attempts cannot exceed 5")
        private Integer retryAttempts;

        /**
         * JSONPath expression for extracting data from JSON responses.
         */
        private String jsonPath;
    }

    /**
     * Configuration for target destination where processed data will be written.
     *
     * <p>Supports two main target types:</p>
     * <ul>
     *     <li><b>Table:</b> Direct database table writing</li>
     *     <li><b>Model:</b> DTO-based processing with optional database writing</li>
     * </ul>
     *
     * <p><b>Performance Configuration:</b></p>
     * <ul>
     *     <li>Batch size controls memory usage and database performance</li>
     *     <li>Schema specification enables multi-tenant scenarios</li>
     * </ul>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TargetConfig {

        /**
         * Database schema name for table targets.
         */
        private String schema;

        /**
         * Target table name or model identifier.
         */
        private String table;

        /**
         * Target type: "table" for direct database writing, "model" for DTO processing.
         */
        @Pattern(regexp = "^(table|model)$",
                message = "Target type must be either 'table' or 'model'")
        private String type = "table"; // Default value

        /**
         * Batch size for database operations (balances memory usage and performance).
         */
        @Min(value = 1, message = "Batch size must be at least 1")
        @Max(value = 10000, message = "Batch size cannot exceed 10000")
        private Integer batchSize;
    }

    /**
     * Configuration for mapping data source columns to target fields with type conversion.
     *
     * <p><b>Mapping Strategies:</b></p>
     * <ul>
     *   <li><b>DIRECT Mapping:</b>
     *     <ul>
     *       <li>Source column names must exactly match the target field names</li>
     *       <li>No need to define columnMapping in YAML configuration</li>
     *       <li>Simple, fast, and requires minimal configuration overhead</li>
     *       <li>Best for scenarios where source and target schemas align</li>
     *     </ul>
     *   </li>
     *   <li><b>MAPPED Strategy:</b>
     *     <ul>
     *       <li>Uses the columnMapping section in YAML to define explicit mappings</li>
     *       <li>Allows renaming and transformation of field names</li>
     *       <li>Supports complex mapping scenarios (nested fields, naming conventions)</li>
     *       <li>Enables data type conversion and validation rules</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * <p><b>Data Type Conversion:</b></p>
     * <ul>
     *     <li><b>STRING:</b> Default type, no conversion applied</li>
     *     <li><b>INTEGER/LONG:</b> Numeric conversion with range validation</li>
     *     <li><b>DOUBLE/BIGDECIMAL:</b> Decimal conversion with precision handling</li>
     *     <li><b>BOOLEAN:</b> Boolean conversion supporting multiple formats</li>
     *     <li><b>LOCALDATE/LOCALDATETIME:</b> Date/time conversion with format specification</li>
     *     <li><b>TIMESTAMP:</b> Database timestamp conversion</li>
     * </ul>
     *
     * <p><b>YAML Configuration Example:</b></p>
     * <pre>{@code
     * mappingStrategy: MAPPED
     * columnMapping:
     *   - source: "currency_pair"   # CSV column name
     *     target: "currency"        # Target field name
     *     dataType: "STRING"
     *     required: true
     *   - source: "trade_date"
     *     target: "tradeDate"
     *     dataType: "LOCALDATE"
     *     sourceDateFormat: "yyyy-MM-dd"
     *     required: true
     * }</pre>
     *
     * <p><b>Strict Mapping Behavior:</b></p>
     * <ul>
     *   <li><b>strictMapping: true (Recommended for Production):</b>
     *     <ul>
     *       <li>Enforces strict field mapping validation</li>
     *       <li>Throws exceptions if any required field is missing or mapping fails</li>
     *       <li>Stops processing immediately on the first error</li>
     *       <li>Ensures data quality and integrity</li>
     *       <li>Provides fast failure for configuration issues</li>
     *     </ul>
     *   </li>
     *   <li><b>strictMapping: false (Useful for Development/Testing):</b>
     *     <ul>
     *       <li>Continues processing even if some fields fail to map</li>
     *       <li>Logs warnings for each failed mapping attempt</li>
     *       <li>Skips problematic records but processes valid ones</li>
     *       <li>Useful for debugging, partial loads, or exploratory data analysis</li>
     *       <li>Enables graceful degradation in data quality scenarios</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * <p><b>Error Handling Examples:</b></p>
     * <pre>{@code
     * // strictMapping: true
     * if (fieldMappingFails) {
     *     throw new ModelConversionException("Required field 'currency' not found");
     * }
     *
     * // strictMapping: false
     * if (fieldMappingFails) {
     *     logger.warn("Field 'currency' mapping failed, skipping record");
     *     return null; // Skip this record and continue processing
     * }
     * }</pre>
     *
     * <p><b>Default Value Handling:</b></p>
     * <ul>
     *     <li>Default values are applied when source fields are missing or null</li>
     *     <li>Type conversion is applied to default values</li>
     *     <li>Required field validation occurs after default value application</li>
     * </ul>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColumnMapping {

        /**
         * Source column name in the input data.
         */
        @NotBlank(message = "Source column name cannot be blank")
        private String source;

        /**
         * Target field name in the output (database column or DTO field).
         */
        @NotBlank(message = "Target column name cannot be blank")
        private String target;

        /**
         * Data type for type conversion and validation.
         */
        @Pattern(regexp = "^(STRING|INTEGER|LONG|DOUBLE|BIGDECIMAL|BOOLEAN|LOCALDATE|LOCALDATETIME|TIMESTAMP)$",
                message = "Invalid data type. Must be one of: STRING, INTEGER, LONG, DOUBLE, BIGDECIMAL, BOOLEAN, LOCALDATE, LOCALDATETIME, TIMESTAMP")
        private String dataType = "STRING"; // Default value

        /**
         * Date format pattern for parsing source date/time values.
         */
        private String sourceDateFormat;

        /**
         * Date format pattern for formatting target date/time values.
         */
        private String targetDateFormat;

        /**
         * Timezone for date/time conversion.
         */
        private String timeZone;

        /**
         * Decimal format pattern for numeric conversion.
         */
        private String decimalFormat;

        /**
         * Whether this field is required (validation will fail if missing).
         */
        private Boolean required = false; // Default value

        /**
         * Default value to use when source field is missing or null.
         */
        private String defaultValue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelConfig {
        @NotBlank(message = "Model class name cannot be blank when target type is 'model'")
        private String className;

        private String packageName;

        @Pattern(regexp = "^(DIRECT|MAPPED)$",
                message = "Mapping strategy must be either 'DIRECT' or 'MAPPED'")
        private String mappingStrategy = "MAPPED"; // Default value

        private Boolean strictMapping = true; // Default value
        private String dateFormat;
        private String timeZone;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationConfig {
        private List<String> requiredColumns;
        private boolean dataQualityChecks;
    }
}
