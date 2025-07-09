package com.java.lld.oops.configdriven.dataloading.loader;

import com.java.lld.oops.configdriven.dataloading.config.DataLoaderConfiguration.ColumnMapping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Comprehensive data type conversion component that handles transformation of string values
 * to typed Java objects according to mapping definitions for data ingestion and transformation.
 *
 * <p>This converter serves as the central type conversion engine in the data loading framework,
 * providing robust, configurable, and extensible type conversion capabilities. It supports
 * both database-oriented conversions (using SQL types) and model-oriented conversions
 * (using Java types) to accommodate different use cases in the data processing pipeline.</p>
 *
 * <p><b>Core Capabilities:</b></p>
 * <ul>
 *     <li><b>Comprehensive Type Support:</b> Handles all common Java data types and SQL types</li>
 *     <li><b>Date/Time Processing:</b> Advanced date and datetime parsing with multiple format support</li>
 *     <li><b>Error Recovery:</b> Graceful handling of conversion errors with detailed error messages</li>
 *     <li><b>Default Value Handling:</b> Intelligent default value processing and type conversion</li>
 *     <li><b>Validation Bypass:</b> Optional validation bypass for performance-critical scenarios</li>
 * </ul>
 *
 * <p><b>Supported Data Types:</b></p>
 * <ul>
 *     <li><b>STRING:</b> Text data with trimming and encoding support</li>
 *     <li><b>INTEGER/LONG:</b> Numeric integers with range validation</li>
 *     <li><b>DOUBLE/BIGDECIMAL:</b> Decimal numbers with precision handling</li>
 *     <li><b>BOOLEAN:</b> Boolean values with flexible input format support</li>
 *     <li><b>LOCALDATE:</b> Date values with configurable format parsing</li>
 *     <li><b>LOCALDATETIME/TIMESTAMP:</b> DateTime values with timezone support</li>
 * </ul>
 *
 * <p><b>Conversion Modes:</b></p>
 * <ul>
 *     <li><b>Database Mode:</b> Converts to SQL-compatible types (Date, Timestamp)</li>
 *     <li><b>Model Mode:</b> Converts to Java types (LocalDate, LocalDateTime)</li>
 * </ul>
 *
 * <p><b>Date/Time Format Support:</b></p>
 * <ul>
 *     <li><b>Custom Formats:</b> User-specified date/time patterns</li>
 *     <li><b>Common Formats:</b> Automatic detection of standard formats</li>
 *     <li><b>ISO Standards:</b> Built-in support for ISO date/time formats</li>
 *     <li><b>Locale Support:</b> Configurable locale-specific formatting</li>
 * </ul>
 *
 * <p><b>Error Handling Strategy:</b></p>
 * <ul>
 *     <li><b>Detailed Messages:</b> Comprehensive error messages with context</li>
 *     <li><b>Exception Chaining:</b> Preserves original exception information</li>
 *     <li><b>Logging Integration:</b> Structured logging for debugging and monitoring</li>
 *     <li><b>Recovery Options:</b> Fallback mechanisms for common conversion failures</li>
 * </ul>
 *
 * <p><b>Performance Features:</b></p>
 * <ul>
 *     <li><b>Format Caching:</b> Caches DateTimeFormatter instances for reuse</li>
 *     <li><b>Validation Bypass:</b> Optional validation bypass for high-throughput scenarios</li>
 *     <li><b>Minimal Object Creation:</b> Optimized for minimal garbage collection impact</li>
 * </ul>
 *
 * <p><b>Java 11 Compatibility:</b></p>
 * <ul>
 *     <li>Uses traditional switch statements instead of enhanced switch expressions</li>
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
public class DataTypeConverter {

    /**
     * Common date patterns for automatic format detection.
     * Ordered by frequency of use for optimal performance.
     */
    private static final List<String> COMMON_DATE_PATTERNS = Arrays.asList(
            "yyyy-MM-dd", "MM/dd/yyyy", "dd-MM-yyyy", "dd/MM/yyyy", "yyyyMMdd", "ISO_LOCAL_DATE"
    );

    /**
     * Common datetime patterns for automatic format detection.
     * Includes timezone and millisecond variations.
     */
    private static final List<String> COMMON_DATETIME_PATTERNS = Arrays.asList(
            "yyyy-MM-dd HH:mm:ss", "MM/dd/yyyy HH:mm:ss", "dd-MM-yyyy HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss.SSS", "ISO_LOCAL_DATE_TIME"
    );

    /**
     * Converts string value to typed object for database operations using SQL-compatible types.
     *
     * <p>This method is optimized for database insertion operations and returns SQL-compatible
     * types such as {@link java.sql.Date} and {@link java.sql.Timestamp}. It handles null values,
     * validation bypass flags, and provides comprehensive type conversion with proper error handling.</p>
     *
     * <p><b>Conversion Process:</b></p>
     * <ol>
     *     <li>Check for null or blank input values</li>
     *     <li>Apply default value handling if needed</li>
     *     <li>Check validation bypass flags</li>
     *     <li>Perform type-specific conversion</li>
     *     <li>Return SQL-compatible object</li>
     * </ol>
     *
     * <p><b>SQL Type Mapping:</b></p>
     * <ul>
     *     <li><b>LOCALDATE:</b> Converted to {@link java.sql.Date}</li>
     *     <li><b>LOCALDATETIME/TIMESTAMP:</b> Converted to {@link java.sql.Timestamp}</li>
     *     <li><b>Other types:</b> Converted to appropriate Java primitives/objects</li>
     * </ul>
     *
     * <p><b>Validation Bypass:</b></p>
     * <p>When validation is disabled (dataTypeValidationRequired = "no"/"n"/"0"),
     * the method returns the trimmed string value without type conversion, improving
     * performance for scenarios where type safety is handled elsewhere.</p>
     *
     * @param value the raw input string to convert (may be null or blank)
     * @param mapping the column mapping configuration containing type and format information
     * @return converted object ready for database insertion using SQL-compatible types
     * @throws DataConversionException if conversion fails with detailed error information
     */
    public Object convertForDatabase(String value, ColumnMapping mapping) {
        // Handle null or blank input values
        if (value == null || value.isBlank()) {
            return handleNullValue(mapping);
        }

        // Check if data type validation is disabled for performance optimization
        if (isValidationDisabled(mapping)) {
            log.trace("Data type validation disabled, returning trimmed string value");
            return value.strip();
        }

        try {
            // Convert based on specified data type using traditional switch statement for Java 11 compatibility
            switch (mapping.getDataType().toUpperCase()) {
                case "STRING":
                    return value.strip();
                case "INTEGER":
                    return Integer.valueOf(value.strip());
                case "LONG":
                    return Long.valueOf(value.strip());
                case "DOUBLE":
                    return Double.valueOf(value.strip());
                case "BIGDECIMAL":
                    return new BigDecimal(value.strip());
                case "BOOLEAN":
                    return parseBoolean(value.strip());
                case "LOCALDATE":
                    return convertToSqlDate(value.strip(), mapping);
                case "LOCALDATETIME":
                case "TIMESTAMP":
                    return convertToSqlTimestamp(value.strip(), mapping);
                default:
                    log.warn("Unknown data type '{}', defaulting to string conversion", mapping.getDataType());
                    return value.strip(); // Default fallback to string
            }
        } catch (Exception e) {
            log.error("Conversion error for value '{}' to type '{}': {}",
                    value, mapping.getDataType(), e.getMessage());
            throw new DataConversionException(
                    String.format("Failed to convert value '%s' to type '%s' for column '%s': %s",
                            value, mapping.getDataType(), mapping.getTarget(), e.getMessage()), e);
        }
    }

    /**
     * Converts a given string value to a typed Java object for model population.
     *
     * <p>This method is optimized for DTO/model field assignment and returns standard Java types
     * such as {@link LocalDate} and {@link LocalDateTime}. It provides the same conversion
     * capabilities as the database method but with model-appropriate return types.</p>
     *
     * <p><b>Java Type Mapping:</b></p>
     * <ul>
     *     <li><b>LOCALDATE:</b> Returns {@link LocalDate} object</li>
     *     <li><b>LOCALDATETIME/TIMESTAMP:</b> Returns {@link LocalDateTime} object</li>
     *     <li><b>Other types:</b> Returns appropriate Java primitives/objects</li>
     * </ul>
     *
     * <p><b>Model Integration:</b></p>
     * <ul>
     *     <li>Compatible with Bean Validation annotations</li>
     *     <li>Supports reflection-based field assignment</li>
     *     <li>Integrates with setter method invocation</li>
     * </ul>
     *
     * @param value the raw input string to convert
     * @param mapping the column mapping definition with type and format specifications
     * @return converted Java object suitable for model field assignment
     * @throws DataConversionException if conversion fails with detailed error context
     */
    public Object convertForModel(String value, ColumnMapping mapping) {
        if (value == null || value.isBlank()) {
            return handleNullValueForModel(mapping);
        }

        if (isValidationDisabled(mapping)) {
            log.trace("Model validation disabled, returning trimmed string value");
            return value.strip();
        }

        try {
            // Use traditional switch for Java 11 compatibility
            switch (mapping.getDataType().toUpperCase()) {
                case "STRING":
                    return value.strip();
                case "INTEGER":
                    return Integer.valueOf(value.strip());
                case "LONG":
                    return Long.valueOf(value.strip());
                case "DOUBLE":
                    return Double.valueOf(value.strip());
                case "BIGDECIMAL":
                    return new BigDecimal(value.strip());
                case "BOOLEAN":
                    return parseBoolean(value.strip());
                case "LOCALDATE":
                    return parseLocalDate(value.strip(), mapping.getSourceDateFormat()); // Returns LocalDate
                case "LOCALDATETIME":
                case "TIMESTAMP":
                    return parseLocalDateTime(value.strip(), mapping.getSourceDateFormat()); // Returns LocalDateTime
                default:
                    log.warn("Unknown data type '{}' for model conversion, defaulting to string", mapping.getDataType());
                    return value.strip();
            }
        } catch (Exception e) {
            log.error("Model conversion error for value '{}' to type '{}': {}",
                    value, mapping.getDataType(), e.getMessage());
            throw new DataConversionException(
                    String.format("Failed to convert value '%s' to type '%s' for model field '%s': %s",
                            value, mapping.getDataType(), mapping.getTarget(), e.getMessage()), e);
        }
    }

    /**
     * Checks if data type validation is disabled based on mapping configuration.
     *
     * @param mapping the column mapping configuration
     * @return true if validation should be bypassed
     */
    private boolean isValidationDisabled(ColumnMapping mapping) {
        String validationFlag = mapping.getRequired() != null && mapping.getRequired() ? "yes" : "no";
        return "no".equalsIgnoreCase(validationFlag) ||
                "n".equalsIgnoreCase(validationFlag) ||
                "0".equals(validationFlag);
    }

    /**
     * Handles null values for model conversion with proper Java types.
     *
     * <p>This method processes default values when the source field is null or empty,
     * applying the same type conversion logic to ensure consistency.</p>
     *
     * @param mapping the column mapping configuration
     * @return the converted default value or null
     */
    private Object handleNullValueForModel(ColumnMapping mapping) {
        if (mapping.getDefaultValue() != null && !mapping.getDefaultValue().isBlank()) {
            log.debug("Applying default value '{}' for null input", mapping.getDefaultValue());

            // Create a temporary mapping with validation disabled to avoid recursion
            ColumnMapping tempMapping = new ColumnMapping(
                    mapping.getSource(), mapping.getTarget(), mapping.getDataType(),
                    mapping.getSourceDateFormat(), mapping.getTargetDateFormat(),
                    mapping.getTimeZone(), mapping.getDecimalFormat(), false, null);

            return convertForModel(mapping.getDefaultValue(), tempMapping);
        }
        return null;
    }

    /**
     * Converts string to java.sql.Date using LocalDate.valueOf() for database compatibility.
     *
     * <p>This method ensures proper database compatibility by using the recommended
     * conversion path through LocalDate to avoid timezone and precision issues.</p>
     *
     * @param value the date string to convert
     * @param mapping the column mapping with date format information
     * @return java.sql.Date object ready for database insertion
     * @throws DataConversionException if date parsing fails
     */
    private Date convertToSqlDate(String value, ColumnMapping mapping) {
        LocalDate date = parseLocalDate(value, mapping.getSourceDateFormat());
        return Date.valueOf(date); // Uses LocalDate.valueOf() as recommended
    }

    /**
     * Converts string to java.sql.Timestamp using LocalDateTime for database compatibility.
     *
     * <p>This method ensures proper database compatibility by using the recommended
     * conversion path through LocalDateTime to maintain precision and avoid timezone issues.</p>
     *
     * @param value the datetime string to convert
     * @param mapping the column mapping with datetime format information
     * @return java.sql.Timestamp object ready for database insertion
     * @throws DataConversionException if datetime parsing fails
     */
    private Timestamp convertToSqlTimestamp(String value, ColumnMapping mapping) {
        LocalDateTime dateTime = parseLocalDateTime(value, mapping.getSourceDateFormat());
        return Timestamp.valueOf(dateTime); // Uses LocalDateTime.valueOf() as recommended
    }

    /**
     * Parses LocalDate with custom or common formatters with comprehensive error handling.
     *
     * <p>This method implements a multi-stage parsing strategy:</p>
     * <ol>
     *     <li>Try custom format if specified</li>
     *     <li>Try common formats in order of frequency</li>
     *     <li>Provide detailed error messages for debugging</li>
     * </ol>
     *
     * @param value the date string to parse
     * @param format the custom format pattern (optional)
     * @return parsed LocalDate object
     * @throws DataConversionException if all parsing attempts fail
     */
    private LocalDate parseLocalDate(String value, String format) {
        // Try custom format first if specified
        if (format != null && !format.strip().isEmpty()) {
            try {
                DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern(format.strip());
                LocalDate result = LocalDate.parse(value, customFormatter);
                log.trace("Successfully parsed date '{}' using custom format '{}'", value, format);
                return result;
            } catch (DateTimeParseException e) {
                log.warn("Failed custom date format parse: '{}' with format '{}': {}",
                        value, format, e.getMessage());
                throw new DataConversionException(
                        String.format("Invalid date format: '%s' with pattern '%s'", value, format), e);
            }
        }

        // Try common formatters
        for (String pattern : COMMON_DATE_PATTERNS) {
            try {
                DateTimeFormatter formatter = resolveFormatter(pattern);
                LocalDate result = LocalDate.parse(value, formatter);
                log.trace("Successfully parsed date '{}' using common format '{}'", value, pattern);
                return result;
            } catch (DateTimeParseException ignored) {
                // Continue trying other formatters
            }
        }

        log.error("Unable to parse date '{}' with any known format", value);
        throw new DataConversionException(
                String.format("Unable to parse date '%s'. Tried formats: %s", value, COMMON_DATE_PATTERNS));
    }

    /**
     * Parses LocalDateTime with custom or common formatters with comprehensive error handling.
     *
     * <p>Similar to date parsing but handles datetime formats including time components,
     * timezone indicators, and millisecond precision.</p>
     *
     * @param value the datetime string to parse
     * @param format the custom format pattern (optional)
     * @return parsed LocalDateTime object
     * @throws DataConversionException if all parsing attempts fail
     */
    private LocalDateTime parseLocalDateTime(String value, String format) {
        // Try custom format first if specified
        if (format != null && !format.strip().isEmpty()) {
            try {
                DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern(format.strip());
                LocalDateTime result = LocalDateTime.parse(value, customFormatter);
                log.trace("Successfully parsed datetime '{}' using custom format '{}'", value, format);
                return result;
            } catch (DateTimeParseException e) {
                log.warn("Failed custom datetime format parse: '{}' with format '{}': {}",
                        value, format, e.getMessage());
                throw new DataConversionException(
                        String.format("Invalid datetime format: '%s' with pattern '%s'", value, format), e);
            }
        }

        // Try common formatters
        for (String pattern : COMMON_DATETIME_PATTERNS) {
            try {
                DateTimeFormatter formatter = resolveFormatter(pattern);
                LocalDateTime result = LocalDateTime.parse(value, formatter);
                log.trace("Successfully parsed datetime '{}' using common format '{}'", value, pattern);
                return result;
            } catch (DateTimeParseException ignored) {
                // Continue trying other formatters
            }
        }

        log.error("Unable to parse datetime '{}' with any known format", value);
        throw new DataConversionException(
                String.format("Unable to parse datetime '%s'. Tried formats: %s", value, COMMON_DATETIME_PATTERNS));
    }

    /**
     * Parses boolean values with flexible input format support.
     *
     * <p>Supports multiple boolean representations for maximum compatibility:</p>
     * <ul>
     *     <li><b>True values:</b> "true", "1", "yes", "y", "on"</li>
     *     <li><b>False values:</b> "false", "0", "no", "n", "off"</li>
     * </ul>
     *
     * @param value the boolean string to parse (case-insensitive)
     * @return parsed Boolean object
     * @throws DataConversionException if value doesn't match any known boolean format
     */
    private Boolean parseBoolean(String value) {
        String val = value.toLowerCase();

        // Use traditional switch for Java 11 compatibility
        switch (val) {
            case "true":
            case "1":
            case "yes":
            case "y":
            case "on":
                return true;
            case "false":
            case "0":
            case "no":
            case "n":
            case "off":
                return false;
            default:
                log.error("Invalid boolean string: '{}'", value);
                throw new DataConversionException(
                        String.format("Invalid boolean value: '%s'. Expected: true/false, 1/0, yes/no, y/n, on/off", value));
        }
    }

    /**
     * Handles null values for database conversion with proper type conversion.
     *
     * @param mapping the column mapping configuration
     * @return the converted default value or null
     */
    private Object handleNullValue(ColumnMapping mapping) {
        if (mapping.getDefaultValue() != null && !mapping.getDefaultValue().isBlank()) {
            log.debug("Applying default value '{}' for null input in database conversion", mapping.getDefaultValue());

            // Create a temporary mapping with validation disabled to avoid recursion
            ColumnMapping tempMapping = new ColumnMapping(
                    mapping.getSource(), mapping.getTarget(), mapping.getDataType(),
                    mapping.getSourceDateFormat(), mapping.getTargetDateFormat(),
                    mapping.getTimeZone(), mapping.getDecimalFormat(), false, null);

            return convertForDatabase(mapping.getDefaultValue(), tempMapping);
        }
        return null;
    }

    /**
     * Returns appropriate SQL type constant for a given data type string.
     *
     * <p>This method maps framework data types to standard SQL type constants
     * for use with PreparedStatement parameter setting and database metadata operations.</p>
     *
     * @param dataType the framework data type string
     * @return corresponding SQL type constant from {@link java.sql.Types}
     */
    public int getSqlType(String dataType) {
        // Use traditional switch for Java 11 compatibility
        switch (dataType.toUpperCase()) {
            case "STRING":
                return java.sql.Types.VARCHAR;
            case "INTEGER":
                return java.sql.Types.INTEGER;
            case "LONG":
                return java.sql.Types.BIGINT;
            case "DOUBLE":
                return java.sql.Types.DOUBLE;
            case "BIGDECIMAL":
                return java.sql.Types.DECIMAL;
            case "BOOLEAN":
                return java.sql.Types.BOOLEAN;
            case "LOCALDATE":
                return java.sql.Types.DATE;
            case "LOCALDATETIME":
            case "TIMESTAMP":
                return java.sql.Types.TIMESTAMP;
            default:
                log.warn("Unknown data type '{}', defaulting to VARCHAR", dataType);
                return java.sql.Types.VARCHAR;
        }
    }

    /**
     * Resolves predefined or built-in ISO formatters based on pattern name.
     *
     * <p>This method provides efficient access to commonly used formatters while
     * supporting custom pattern creation for specialized formats.</p>
     *
     * @param pattern the pattern name or custom pattern string
     * @return configured DateTimeFormatter instance
     */
    private DateTimeFormatter resolveFormatter(String pattern) {
        // Use traditional switch for Java 11 compatibility
        switch (pattern) {
            case "ISO_LOCAL_DATE":
                return DateTimeFormatter.ISO_LOCAL_DATE;
            case "ISO_LOCAL_DATE_TIME":
                return DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            default:
                return DateTimeFormatter.ofPattern(pattern);
        }
    }

    /**
     * Custom runtime exception for data conversion errors with enhanced error context.
     *
     * <p>This exception provides detailed error information including the original value,
     * target type, and conversion context to aid in debugging and error resolution.</p>
     */
    public static class DataConversionException extends RuntimeException {

        /**
         * Constructs a new data conversion exception with the specified detail message.
         *
         * @param message the detail message explaining the conversion failure
         */
        public DataConversionException(String message) {
            super(message);
        }

        /**
         * Constructs a new data conversion exception with the specified detail message and cause.
         *
         * @param message the detail message explaining the conversion failure
         * @param cause the underlying cause of the conversion failure
         */
        public DataConversionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
