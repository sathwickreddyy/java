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
@ConfigurationProperties(prefix = "data-sources")
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

            @NotNull(message = "Target configuration cannot be null")
            @Valid
            TargetConfig target,

            @NotNull(message = "Column mapping cannot be null")
            @NotEmpty(message = "Column mapping cannot be empty")
            @Valid
            List<ColumnMapping> columnMapping,

            ValidationConfig validation
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

            // Validation options
            Boolean required,
            String defaultValue
    ) {
        // Default constructor with sensible defaults
        public ColumnMapping {
            if (dataType == null) {
                dataType = "STRING";
            }
            if (required == null) {
                required = false;
            }
        }
    }

    public record ValidationConfig(
            List<String> requiredColumns,
            boolean dataQualityChecks
    ) {}
}
