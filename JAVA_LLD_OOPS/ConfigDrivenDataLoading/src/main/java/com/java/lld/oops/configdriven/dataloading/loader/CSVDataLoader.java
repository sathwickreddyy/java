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
 * {@code CSVDataLoader} reads data from a CSV file and converts each row into a {@link DataRecord}.
 * <p>
 * It supports configuration of delimiter, character encoding, and whether the CSV has a header.
 * It uses Apache Commons CSV for parsing and SLF4J for logging.
 *
 * @author sathwick
 */
@Slf4j
@Component
public class CSVDataLoader implements DataLoader {

    /**
     * Returns the data source type handled by this loader.
     *
     * @return "CSV"
     */
    @Override
    public String getType() {
        return "CSV";
    }

    /**
     * Loads CSV data based on the provided configuration and returns a stream of {@link DataRecord}.
     * Proper error handling ensures that malformed rows are logged and converted into invalid records.
     *
     * @param config the data source configuration containing file path, delimiter, encoding, and header flag
     * @return a stream of {@link DataRecord} parsed from the CSV file
     */
    @Override
    public Stream<DataRecord> loadData(DataLoaderConfiguration.DataSourceDefinition config) {
        var source = config.source();
        String filePath = source.filePath().strip();
        String encoding = source.encoding().strip();
        char delimiter = source.delimiter().strip().charAt(0);

        log.info("Loading CSV file from path: {}", filePath);

        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            log.error("CSV file not found: {}", filePath);
            throw new IllegalArgumentException("CSV file not found: " + filePath);
        }

        try {
            // Build CSV format
            CSVFormat format = CSVFormat.Builder.create()
                    .setDelimiter(delimiter)
                    .setSkipHeaderRecord(false)
                    .build();

            if (source.header()) {
                format = CSVFormat.Builder.create(format)
                        .setHeader() // Use first row as header
                        .setIgnoreEmptyLines(true)
                        .setIgnoreSurroundingSpaces(true)
                        .build();
                log.info("CSV header is enabled. First row will be treated as header.");
            }

            // Create parser
            CSVParser parser = new CSVParser(
                    new FileReader(file, Charset.forName(encoding)),
                    format
            );

            var rowCounter = new AtomicInteger(0);

            // Stream records
            return StreamSupport.stream(parser.spliterator(), false)
                    .map(record -> convertToDataRecord(record, rowCounter.incrementAndGet()))
                    .onClose(() -> {
                        try {
                            parser.close();
                            log.info("CSV parser closed after reading {} rows.", rowCounter.get());
                        } catch (IOException e) {
                            log.error("Failed to close CSV parser", e);
                        }
                    });

        } catch (IOException e) {
            log.error("I/O error while reading CSV file: {}", filePath, e);
            throw new RuntimeException("I/O error while reading CSV file: " + filePath, e);
        } catch (Exception e) {
            log.error("Unexpected error while loading CSV data from file: {}", filePath, e);
            throw new RuntimeException("Unexpected error while loading CSV data", e);
        }
    }

    /**
     * Converts a {@link CSVRecord} to a {@link DataRecord}, handling any malformed data gracefully.
     *
     * @param record    the CSV record to convert
     * @param rowNumber the row number (1-based index) for tracking and debugging
     * @return a {@link DataRecord} representing the parsed row (valid or invalid)
     */
    private DataRecord convertToDataRecord(CSVRecord record, int rowNumber) {
        try {
            Map<String, Object> data = new HashMap<>();
            record.toMap().forEach(data::put);
            return DataRecord.valid(data, rowNumber);
        } catch (Exception e) {
            log.warn("Failed to parse CSV record at row {}: {} | Error: {}", rowNumber, record, e.getMessage());
            return DataRecord.invalid(Map.of(), rowNumber, "Error parsing CSV record: " + e.getMessage());
        }
    }
}