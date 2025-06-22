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
 * Component that handles conversion of string values to typed Java objects
 * according to mapping definitions for use in data ingestion and transformation.
 *
 * @author sathwick
 */
@Slf4j
@Component
public class DataTypeConverter {

    private static final List<String> COMMON_DATE_PATTERNS = List.of(
            "yyyy-MM-dd", "MM/dd/yyyy", "dd-MM-yyyy", "dd/MM/yyyy", "yyyyMMdd", "ISO_LOCAL_DATE"
    );

    private static final List<String> COMMON_DATETIME_PATTERNS = List.of(
            "yyyy-MM-dd HH:mm:ss", "MM/dd/yyyy HH:mm:ss", "dd-MM-yyyy HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss", "ISO_LOCAL_DATE_TIME"
    );

    /**
     * Converts a given string value to a typed object based on column mapping.
     *
     * @param value   the raw input string
     * @param mapping the column mapping definition
     * @return converted Java object for database ingestion
     */
    public Object convertForDatabase(String value, ColumnMapping mapping) {
        if (value == null || value.isBlank()) {
            return handleNullValue(mapping);
        }
        if ("no".equalsIgnoreCase(mapping.dataTypeValidationRequired()) ||
            "n".equalsIgnoreCase(mapping.dataTypeValidationRequired()) ||
            "0".equals(mapping.dataTypeValidationRequired())) {
            return value.strip();
        }
        try {
            return switch (mapping.dataType().toUpperCase()) {
                case "STRING" -> value.strip();
                case "INTEGER" -> Integer.valueOf(value.strip());
                case "LONG" -> Long.valueOf(value.strip());
                case "DOUBLE" -> Double.valueOf(value.strip());
                case "BIGDECIMAL" -> new BigDecimal(value.strip());
                case "BOOLEAN" -> parseBoolean(value.strip());
                case "LOCALDATE" -> convertToSqlDate(value.strip(), mapping);
                case "LOCALDATETIME", "TIMESTAMP" -> convertToSqlTimestamp(value.strip(), mapping);
                default -> value.strip(); // default as string
            };
        } catch (Exception e) {
            log.error("Conversion error for value '{}' to type '{}': {}", value, mapping.dataType(), e.getMessage());
            throw new DataConversionException(
                    String.format("Failed to convert value '%s' to type '%s' for column '%s'",
                            value, mapping.dataType(), mapping.target()), e);
        }
    }

    private Date convertToSqlDate(String value, ColumnMapping mapping) {
        LocalDate date = parseLocalDate(value, mapping.sourceDateFormat());
        return Date.valueOf(date);
    }

    private Timestamp convertToSqlTimestamp(String value, ColumnMapping mapping) {
        LocalDateTime dateTime = parseLocalDateTime(value, mapping.sourceDateFormat());
        return Timestamp.valueOf(dateTime);
    }

    private LocalDate parseLocalDate(String value, String format) {
        if (format != null && !format.strip().isEmpty()) {
            try {
                return LocalDate.parse(value, DateTimeFormatter.ofPattern(format.strip()));
            } catch (DateTimeParseException e) {
                log.warn("Failed custom date format parse: {} with format {}", value, format);
                throw new DataConversionException("Invalid date format: " + value, e);
            }
        }

        for (String pattern : COMMON_DATE_PATTERNS) {
            try {
                DateTimeFormatter formatter = resolveFormatter(pattern);
                return LocalDate.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        log.error("Unable to parse date '{}'", value);
        throw new DataConversionException("Unable to parse date: " + value);
    }

    private LocalDateTime parseLocalDateTime(String value, String format) {
        if (format != null && !format.strip().isEmpty()) {
            try {
                return LocalDateTime.parse(value, DateTimeFormatter.ofPattern(format.strip()));
            } catch (DateTimeParseException e) {
                log.warn("Failed custom datetime format parse: {} with format {}", value, format);
                throw new DataConversionException("Invalid datetime format: " + value, e);
            }
        }

        for (String pattern : COMMON_DATETIME_PATTERNS) {
            try {
                DateTimeFormatter formatter = resolveFormatter(pattern);
                return LocalDateTime.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        log.error("Unable to parse datetime '{}'", value);
        throw new DataConversionException("Unable to parse datetime: " + value);
    }

    private Boolean parseBoolean(String value) {
        String val = value.toLowerCase();
        return switch (val) {
            case "true", "1", "yes", "y", "on" -> true;
            case "false", "0", "no", "n", "off" -> false;
            default -> {
                log.error("Invalid boolean string: '{}'", value);
                throw new DataConversionException("Invalid boolean value: " + value);
            }
        };
    }

    private Object handleNullValue(ColumnMapping mapping) {
        if (mapping.defaultValue() != null && !mapping.defaultValue().isBlank()) {
            return convertForDatabase(mapping.defaultValue(), new ColumnMapping(
                    mapping.source(), mapping.target(), mapping.dataType(),
                    mapping.sourceDateFormat(), mapping.targetDateFormat(),
                    mapping.timeZone(), mapping.decimalFormat(), "no", null));
        }
        return null;
    }

    /**
     * Returns appropriate SQL type for a given data type string.
     */
    public int getSqlType(String dataType) {
        return switch (dataType.toUpperCase()) {
            case "STRING" -> java.sql.Types.VARCHAR;
            case "INTEGER" -> java.sql.Types.INTEGER;
            case "LONG" -> java.sql.Types.BIGINT;
            case "DOUBLE" -> java.sql.Types.DOUBLE;
            case "BIGDECIMAL" -> java.sql.Types.DECIMAL;
            case "BOOLEAN" -> java.sql.Types.BOOLEAN;
            case "LOCALDATE" -> java.sql.Types.DATE;
            case "LOCALDATETIME", "TIMESTAMP" -> java.sql.Types.TIMESTAMP;
            default -> java.sql.Types.VARCHAR;
        };
    }

    /**
     * Resolves predefined or built-in ISO formatters based on pattern name.
     */
    private DateTimeFormatter resolveFormatter(String pattern) {
        return switch (pattern) {
            case "ISO_LOCAL_DATE" -> DateTimeFormatter.ISO_LOCAL_DATE;
            case "ISO_LOCAL_DATE_TIME" -> DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            default -> DateTimeFormatter.ofPattern(pattern);
        };
    }

    /**
     * Runtime exception for data conversion errors with optional cause.
     */
    public static class DataConversionException extends RuntimeException {
        public DataConversionException(String message) {
            super(message);
        }

        public DataConversionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}