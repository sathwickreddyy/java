package com.java.lld.oops.configdriven.dataloading.config;


import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

/**
 * Configuration properties for data loading
 * @param dataSources load-abc-data from data-sources.yaml
 * @author sathwick
 */
@Validated
@ConfigurationProperties(prefix = "load-abc-data")
public record DataLoaderConfiguration(
        @NotNull
        @NotEmpty
        @Valid
        Map<String, DataSourceConfiguration> dataSources
) {

    public record DataSourceConfiguration(
            @NotBlank(message = "Identifier cannot be blank")
            String identifier,

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
            List<ColumnMapping> columnMapping
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
            @NotBlank(message = "Schema cannot be blank")
            String schema,

            @NotBlank(message = "Table cannot be blank")
            String table,

            @Min(value = 1, message = "Batch size must be at least 1")
            @Max(value = 10000, message = "Batch size cannot exceed 10000")
            Integer batchSize
    ) {}

    public record ColumnMapping(
            @NotBlank(message = "Source column cannot be blank")
            String source,
            @NotBlank(message = "Target column cannot be blank")
            String target
    ) {}
}
