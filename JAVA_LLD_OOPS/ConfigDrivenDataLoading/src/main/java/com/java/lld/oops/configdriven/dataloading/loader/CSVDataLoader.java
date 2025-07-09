package com.java.lld.oops.configdriven.dataloading.loader;

import com.java.lld.oops.configdriven.dataloading.config.DataLoaderConfiguration;
import com.java.lld.oops.configdriven.dataloading.model.DataRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * High-performance CSV data loader that reads CSV files and converts each row into a {@link DataRecord}.
 *
 * <p>This loader provides comprehensive CSV processing capabilities with robust error handling,
 * configurable parsing options, and memory-efficient streaming for large datasets. It serves as
 * the primary component for CSV-based data ingestion in the config-driven data loading framework.</p>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *     <li><b>Configurable Parsing:</b> Support for custom delimiters, character encodings, and header handling</li>
 *     <li><b>Stream Processing:</b> Memory-efficient processing of large CSV files using Java streams</li>
 *     <li><b>Error Resilience:</b> Graceful handling of malformed rows with detailed error reporting</li>
 *     <li><b>Resource Management:</b> Automatic cleanup of file resources and parsers</li>
 *     <li><b>Performance Optimization:</b> Lazy evaluation and minimal object creation for optimal throughput</li>
 * </ul>
 *
 * <p><b>Supported CSV Features:</b></p>
 * <ul>
 *     <li><b>Header Support:</b> First row can be treated as column headers</li>
 *     <li><b>Custom Delimiters:</b> Configurable field separators (comma, semicolon, tab, etc.)</li>
 *     <li><b>Character Encoding:</b> Support for various encodings (UTF-8, ISO-8859-1, etc.)</li>
 *     <li><b>Empty Line Handling:</b> Automatic filtering of empty lines when headers are enabled</li>
 *     <li><b>Whitespace Trimming:</b> Optional trimming of surrounding whitespace</li>
 * </ul>
 *
 * <p><b>Error Handling Strategy:</b></p>
 * <ul>
 *     <li><b>File Validation:</b> Validates file existence and readability before processing</li>
 *     <li><b>Malformed Row Recovery:</b> Converts parsing errors into invalid DataRecord objects</li>
 *     <li><b>Resource Cleanup:</b> Ensures proper cleanup even when exceptions occur</li>
 *     <li><b>Detailed Logging:</b> Comprehensive logging for debugging and monitoring</li>
 * </ul>
 *
 * <p><b>Performance Characteristics:</b></p>
 * <ul>
 *     <li><b>Memory Efficient:</b> Processes files larger than available memory through streaming</li>
 *     <li><b>Lazy Loading:</b> Records are processed only when consumed from the stream</li>
 *     <li><b>Minimal Overhead:</b> Direct conversion from CSV records to DataRecord objects</li>
 * </ul>
 *
 * <p><b>Java 11 Compatibility:</b></p>
 * <ul>
 *     <li>Uses traditional getter methods instead of record accessors</li>
 *     <li>Compatible with Java 11, 17, and 21</li>
 *     <li>No behavioral changes from higher Java versions</li>
 *     <li>Maintains performance characteristics across Java versions</li>
 * </ul>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * // Configuration in YAML
 * source:
 *   filePath: "/data/market_data.csv"
 *   delimiter: ","
 *   header: true
 *   encoding: "UTF-8"
 *
 * // Programmatic usage
 * CSVDataLoader loader = new CSVDataLoader();
 * Stream<DataRecord> records = loader.loadData(config);
 * }</pre>
 *
 * @author sathwick
 * @since 1.0.0
 */
@Slf4j
@Component
public class CSVDataLoader implements DataLoader {

    /**
     * Returns the data source type identifier handled by this loader.
     *
     * <p>This identifier is used by the data source factory to select the appropriate
     * loader implementation based on the configuration type specified in YAML files.</p>
     *
     * @return "CSV" - the type identifier for CSV data sources
     */
    @Override
    public String getType() {
        return "CSV";
    }

    /**
     * Loads CSV data based on the provided configuration and returns a stream of {@link DataRecord} objects.
     *
     * <p>This method orchestrates the complete CSV loading process:</p>
     * <ol>
     *     <li><b>Configuration Extraction:</b> Extracts file path, encoding, and delimiter settings</li>
     *     <li><b>File Validation:</b> Validates file existence and accessibility</li>
     *     <li><b>Parser Configuration:</b> Sets up CSV parser with specified options</li>
     *     <li><b>Stream Creation:</b> Creates a lazy-evaluated stream of DataRecord objects</li>
     *     <li><b>Resource Management:</b> Ensures proper cleanup when stream is closed</li>
     * </ol>
     *
     * <p><b>Configuration Parameters:</b></p>
     * <ul>
     *     <li><b>filePath:</b> Path to the CSV file (required)</li>
     *     <li><b>delimiter:</b> Field separator character (default: comma)</li>
     *     <li><b>header:</b> Whether first row contains headers (default: false)</li>
     *     <li><b>encoding:</b> Character encoding (default: UTF-8)</li>
     * </ul>
     *
     * <p><b>Error Handling:</b></p>
     * <ul>
     *     <li>File not found errors result in IllegalArgumentException</li>
     *     <li>I/O errors are wrapped in RuntimeException with context</li>
     *     <li>Malformed rows are converted to invalid DataRecord objects</li>
     *     <li>Parser cleanup is guaranteed through stream onClose handler</li>
     * </ul>
     *
     * <p><b>Stream Characteristics:</b></p>
     * <ul>
     *     <li><b>Lazy Evaluation:</b> Records are parsed only when consumed</li>
     *     <li><b>Sequential Processing:</b> Records are processed in file order</li>
     *     <li><b>Resource Cleanup:</b> Automatic parser cleanup when stream is closed</li>
     *     <li><b>Exception Safety:</b> Malformed records don't break the stream</li>
     * </ul>
     *
     * @param config the data source configuration containing file path, delimiter, encoding, and header flag
     * @return a stream of {@link DataRecord} objects parsed from the CSV file
     * @throws IllegalArgumentException if the specified file does not exist or is not readable
     * @throws RuntimeException if I/O errors occur during file reading or parser initialization
     */
    @Override
    public Stream<DataRecord> loadData(DataLoaderConfiguration.DataSourceDefinition config) {
        // Extract configuration using Java 11 compatible getter methods
        var source = config.getSource();
        String filePath = source.getFilePath().strip();
        String encoding = source.getEncoding() != null ? source.getEncoding().strip() : "UTF-8";
        String delimiterStr = source.getDelimiter() != null ? source.getDelimiter().strip() : ",";
        char delimiter = delimiterStr.charAt(0);
        boolean hasHeader = source.getHeader() != null ? source.getHeader() : false;

        log.info("Loading CSV file from path: {} with encoding: {}, delimiter: '{}', header: {}",
                filePath, encoding, delimiter, hasHeader);

        // Validate file existence and accessibility
        File file = validateFile(filePath);

        try {
            // Configure CSV parser with specified options
            CSVFormat format = buildCSVFormat(delimiter, hasHeader);

            // Create parser with proper encoding
            CSVParser parser = createCSVParser(file, encoding, format);

            // Create row counter for tracking
            var rowCounter = new AtomicInteger(0);

            log.debug("CSV parser created successfully for file: {}", filePath);

            // Create and return stream with proper resource management
            return StreamSupport.stream(parser.spliterator(), false)
                    .map(record -> convertToDataRecord(record, rowCounter.incrementAndGet()))
                    .onClose(() -> cleanupParser(parser, rowCounter.get()));

        } catch (IOException e) {
            log.error("I/O error while reading CSV file: {}", filePath, e);
            throw new RuntimeException("I/O error while reading CSV file: " + filePath, e);
        } catch (Exception e) {
            log.error("Unexpected error while loading CSV data from file: {}", filePath, e);
            throw new RuntimeException("Unexpected error while loading CSV data from: " + filePath, e);
        }
    }

    /**
     * Validates that the specified file exists and is readable.
     *
     * @param filePath the path to the CSV file
     * @return the validated File object
     * @throws IllegalArgumentException if file doesn't exist or isn't readable
     */
    private File validateFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("CSV file path cannot be null or empty");
        }

        File file = new File(filePath);
        if (!file.exists()) {
            log.error("CSV file not found: {}", filePath);
            throw new IllegalArgumentException("CSV file not found: " + filePath);
        }

        if (!file.isFile()) {
            log.error("Path does not point to a file: {}", filePath);
            throw new IllegalArgumentException("Path is not a file: " + filePath);
        }

        if (!file.canRead()) {
            log.error("CSV file is not readable: {}", filePath);
            throw new IllegalArgumentException("CSV file is not readable: " + filePath);
        }

        log.debug("File validation passed for: {} (size: {} bytes)", filePath, file.length());
        return file;
    }

    /**
     * Builds CSV format configuration based on provided parameters.
     *
     * @param delimiter the field delimiter character
     * @param hasHeader whether the first row contains headers
     * @return configured CSVFormat object
     */
    private CSVFormat buildCSVFormat(char delimiter, boolean hasHeader) {
        CSVFormat.Builder formatBuilder = CSVFormat.Builder.create()
                .setDelimiter(delimiter)
                .setSkipHeaderRecord(false);

        if (hasHeader) {
            formatBuilder.setHeader() // Use first row as header
                    .setIgnoreEmptyLines(true)
                    .setIgnoreSurroundingSpaces(true);
            log.debug("CSV header processing enabled with empty line filtering and whitespace trimming");
        }

        return formatBuilder.build();
    }

    /**
     * Creates a CSV parser with the specified file, encoding, and format.
     *
     * @param file the CSV file to parse
     * @param encoding the character encoding to use
     * @param format the CSV format configuration
     * @return configured CSVParser
     * @throws IOException if file reading fails
     */
    private CSVParser createCSVParser(File file, String encoding, CSVFormat format) throws IOException {
        return new CSVParser(
                new FileReader(file, Charset.forName(encoding)),
                format
        );
    }

    /**
     * Cleans up parser resources and logs completion statistics.
     *
     * @param parser the CSV parser to close
     * @param rowCount the number of rows processed
     */
    private void cleanupParser(CSVParser parser, int rowCount) {
        try {
            parser.close();
            log.info("CSV parser closed successfully after reading {} rows", rowCount);
        } catch (IOException e) {
            log.error("Failed to close CSV parser cleanly", e);
        }
    }

    /**
     * Converts a {@link CSVRecord} to a {@link DataRecord}, handling any malformed data gracefully.
     *
     * <p>This method performs the core transformation from CSV format to the framework's
     * internal DataRecord format. It includes comprehensive error handling to ensure that
     * malformed rows don't break the entire processing pipeline.</p>
     *
     * <p><b>Conversion Process:</b></p>
     * <ol>
     *     <li>Extract all key-value pairs from the CSV record</li>
     *     <li>Create a map containing the field data</li>
     *     <li>Wrap the data in a valid DataRecord object</li>
     *     <li>Handle any parsing errors by creating invalid DataRecord</li>
     * </ol>
     *
     * <p><b>Error Recovery:</b></p>
     * <ul>
     *     <li>Parsing exceptions are caught and logged</li>
     *     <li>Invalid records are created with error messages</li>
     *     <li>Processing continues for subsequent records</li>
     * </ul>
     *
     * @param record the CSV record to convert
     * @param rowNumber the row number (1-based index) for tracking and debugging purposes
     * @return a {@link DataRecord} representing the parsed row (valid or invalid)
     */
    private DataRecord convertToDataRecord(CSVRecord record, int rowNumber) {
        try {
            Map<String, Object> data = new HashMap<>();

            // Convert CSV record to map - this may throw exceptions for malformed records
            Map<String, String> recordMap = record.toMap();
            recordMap.forEach(data::put);

            log.trace("Successfully converted CSV record {} with {} fields", rowNumber, data.size());
            return DataRecord.valid(data, rowNumber);

        } catch (Exception e) {
            String errorMessage = "Error parsing CSV record: " + e.getMessage();
            log.warn("Failed to parse CSV record at row {}: {} | Error: {}",
                    rowNumber, record, e.getMessage());

            return DataRecord.invalid(Map.of(), rowNumber, errorMessage);
        }
    }
}
