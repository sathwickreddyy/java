package com.java.lld.oops.configdriven.dataloading.utils;

import com.java.lld.oops.configdriven.dataloading.model.DataRecord;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Comprehensive utility class for printing DataRecord instances in a formatted tabular display
 * with enhanced readability, debugging support, and performance optimization.
 *
 * <p>This utility provides professional-grade tabular output formatting for DataRecord collections,
 * enabling developers to quickly visualize and debug data processing results. It automatically
 * handles column width calculation, data alignment, and null value representation for optimal
 * readability.</p>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *     <li><b>Dynamic Column Sizing:</b> Automatically calculates optimal column widths</li>
 *     <li><b>Null Value Handling:</b> Graceful handling of null and missing values</li>
 *     <li><b>Error Visualization:</b> Clear display of record validation errors</li>
 *     <li><b>Performance Optimized:</b> Efficient processing of large record collections</li>
 *     <li><b>Flexible Formatting:</b> Consistent alignment and spacing for readability</li>
 * </ul>
 *
 * <p><b>Output Format:</b></p>
 * <pre>
 * Row  Valid  Error  column1  column2  column3
 * ---  -----  -----  -------  -------  -------
 * 1    true   -      value1   value2   value3
 * 2    false  Error  value4   value5   value6
 * </pre>
 *
 * <p><b>Use Cases:</b></p>
 * <ul>
 *     <li><b>Development Testing:</b> Quick visualization of data processing results</li>
 *     <li><b>Debugging:</b> Identifying data quality issues and validation errors</li>
 *     <li><b>Data Validation:</b> Manual verification of transformation results</li>
 *     <li><b>Troubleshooting:</b> Analyzing failed records and error patterns</li>
 * </ul>
 *
 * <p><b>Performance Characteristics:</b></p>
 * <ul>
 *     <li><b>Memory Efficient:</b> Processes records in a single pass</li>
 *     <li><b>Scalable:</b> Handles large record collections efficiently</li>
 *     <li><b>Fast Rendering:</b> Optimized string building and formatting</li>
 * </ul>
 *
 * <p><b>Java 11 Compatibility:</b></p>
 * <ul>
 *     <li>Uses traditional getter methods instead of record accessors</li>
 *     <li>String repeat functionality for Java 11 compatibility</li>
 *     <li>Compatible with Java 11, 17, and 21</li>
 *     <li>No behavioral differences across Java versions</li>
 * </ul>
 *
 * @author sathwick
 * @since 1.0.0
 */
@Slf4j
public class DataRecordPrinter {

    // Private constructor to prevent instantiation
    private DataRecordPrinter() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Prints a list of DataRecord objects in a professionally formatted tabular display.
     *
     * <p>This method provides comprehensive tabular formatting with the following features:</p>
     * <ul>
     *     <li><b>Automatic Column Detection:</b> Discovers all columns across all records</li>
     *     <li><b>Dynamic Width Calculation:</b> Calculates optimal column widths for readability</li>
     *     <li><b>Header Generation:</b> Creates clear column headers with metadata</li>
     *     <li><b>Data Alignment:</b> Properly aligns data for consistent presentation</li>
     *     <li><b>Error Highlighting:</b> Clearly displays validation errors and invalid records</li>
     * </ul>
     *
     * <p><b>Column Layout:</b></p>
     * <ol>
     *     <li><b>Row:</b> Sequential row number for tracking</li>
     *     <li><b>Valid:</b> Boolean indicator of record validity</li>
     *     <li><b>Error:</b> Error message for invalid records (or "-" for valid records)</li>
     *     <li><b>Data Columns:</b> All unique columns found across all records</li>
     * </ol>
     *
     * <p><b>Null Value Handling:</b></p>
     * <ul>
     *     <li>Null values are displayed as "-" for clarity</li>
     *     <li>Missing columns are filled with "-" for consistency</li>
     *     <li>Empty strings are preserved as-is</li>
     * </ul>
     *
     * <p><b>Performance Considerations:</b></p>
     * <ul>
     *     <li>Single-pass processing for optimal performance</li>
     *     <li>Efficient string building to minimize memory allocation</li>
     *     <li>Optimized column width calculation</li>
     * </ul>
     *
     * <p><b>Example Output:</b></p>
     * <pre>
     * Row  Valid  Error           trade_date  currency  exchange_rate
     * ---  -----  --------------  ----------  --------  -------------
     * 1    true   -               2024-01-01  USD/EUR   0.8500
     * 2    false  Invalid format  2024-01-02  USD/GBP   invalid
     * 3    true   -               2024-01-03  EUR/JPY   130.45
     * </pre>
     *
     * @param records List of processed DataRecord objects to display in tabular format
     * @throws IllegalArgumentException if records list is null (empty list is handled gracefully)
     */
    public static void printRecords(List<DataRecord> records) {
        if (records == null) {
            throw new IllegalArgumentException("Records list cannot be null");
        }

        if (records.isEmpty()) {
            log.info("No records to print - empty record list provided");
            System.out.println("No records to display.");
            return;
        }

        log.info("Preparing to print {} records in tabular format", records.size());

        try {
            // Step 1: Collect all possible column names across all records
            Set<String> allColumns = collectAllColumns(records);

            // Step 2: Build complete header list with metadata columns
            List<String> headers = buildHeaderList(allColumns);

            // Step 3: Calculate optimal column widths for formatting
            Map<String, Integer> columnWidths = calculateColumnWidths(records, headers, allColumns);

            // Step 4: Render the formatted table
            renderTable(records, headers, columnWidths, allColumns);

            log.info("Successfully printed {} records in tabular format", records.size());

        } catch (Exception e) {
            log.error("Error occurred while printing records: {}", e.getMessage(), e);
            System.out.println("Error displaying records: " + e.getMessage());
        }
    }

    /**
     * Collects all unique column names from all records in the collection.
     *
     * @param records the list of records to analyze
     * @return ordered set of all column names found
     */
    private static Set<String> collectAllColumns(List<DataRecord> records) {
        Set<String> allColumns = new LinkedHashSet<>();

        for (DataRecord record : records) {
            if (record.getData() != null) {
                allColumns.addAll(record.getData().keySet());
            }
        }

        log.debug("Discovered {} unique columns across {} records", allColumns.size(), records.size());
        return allColumns;
    }

    /**
     * Builds the complete header list including metadata and data columns.
     *
     * @param allColumns the set of all data columns
     * @return ordered list of all headers
     */
    private static List<String> buildHeaderList(Set<String> allColumns) {
        List<String> headers = new ArrayList<>();

        // Add metadata columns first
        headers.add("Row");
        headers.add("Valid");
        headers.add("Error");

        // Add all data columns
        headers.addAll(allColumns);

        return headers;
    }

    /**
     * Calculates optimal column widths for proper alignment and readability.
     *
     * @param records the list of records to analyze
     * @param headers the list of column headers
     * @param allColumns the set of all data columns
     * @return map of column names to their optimal widths
     */
    private static Map<String, Integer> calculateColumnWidths(List<DataRecord> records,
                                                              List<String> headers,
                                                              Set<String> allColumns) {
        Map<String, Integer> columnWidths = new LinkedHashMap<>();

        // Initialize with header lengths
        for (String header : headers) {
            columnWidths.put(header, header.length());
        }

        // Calculate maximum width needed for each column based on data
        for (DataRecord record : records) {
            // Update metadata column widths
            updateMetadataColumnWidths(columnWidths, record);

            // Update data column widths
            updateDataColumnWidths(columnWidths, record, allColumns);
        }

        log.debug("Calculated column widths: {}", columnWidths);
        return columnWidths;
    }

    /**
     * Updates column widths for metadata columns (Row, Valid, Error).
     *
     * @param columnWidths the map of column widths to update
     * @param record the record to analyze
     */
    private static void updateMetadataColumnWidths(Map<String, Integer> columnWidths, DataRecord record) {
        // Row number width
        String rowValue = String.valueOf(record.getRowNumber());
        columnWidths.compute("Row", (k, v) -> Math.max(v, rowValue.length()));

        // Valid flag width
        String validValue = String.valueOf(record.isValid());
        columnWidths.compute("Valid", (k, v) -> Math.max(v, validValue.length()));

        // Error message width
        String errorValue = record.getErrorMessage() != null ? record.getErrorMessage() : "-";
        columnWidths.compute("Error", (k, v) -> Math.max(v, errorValue.length()));
    }

    /**
     * Updates column widths for data columns.
     *
     * @param columnWidths the map of column widths to update
     * @param record the record to analyze
     * @param allColumns the set of all data columns
     */
    private static void updateDataColumnWidths(Map<String, Integer> columnWidths,
                                               DataRecord record,
                                               Set<String> allColumns) {
        for (String column : allColumns) {
            Object value = record.getData() != null ? record.getData().get(column) : null;
            String displayValue = value != null ? value.toString() : "-";
            columnWidths.compute(column, (k, v) -> Math.max(v, displayValue.length()));
        }
    }

    /**
     * Renders the complete formatted table to system output.
     *
     * @param records the list of records to display
     * @param headers the list of column headers
     * @param columnWidths the map of column widths
     * @param allColumns the set of all data columns
     */
    private static void renderTable(List<DataRecord> records,
                                    List<String> headers,
                                    Map<String, Integer> columnWidths,
                                    Set<String> allColumns) {
        // Print table header
        printTableHeader(headers, columnWidths);

        // Print separator line
        printSeparatorLine(columnWidths);

        // Print each data row
        for (DataRecord record : records) {
            printDataRow(record, headers, columnWidths, allColumns);
        }

        // Print final separator line
        printSeparatorLine(columnWidths);
    }

    /**
     * Prints the table header row with proper formatting.
     *
     * @param headers the list of column headers
     * @param columnWidths the map of column widths
     */
    private static void printTableHeader(List<String> headers, Map<String, Integer> columnWidths) {
        StringBuilder headerBuilder = new StringBuilder();

        for (String header : headers) {
            int width = columnWidths.get(header);
            headerBuilder.append(String.format("%-" + width + "s", header)).append("  ");
        }

        System.out.println(headerBuilder.toString().stripTrailing());
    }

    /**
     * Prints a separator line using dashes.
     *
     * @param columnWidths the map of column widths
     */
    private static void printSeparatorLine(Map<String, Integer> columnWidths) {
        StringBuilder separatorBuilder = new StringBuilder();

        for (int width : columnWidths.values()) {
            // Java 11 compatible string repeat
            separatorBuilder.append(repeatString("-", width)).append("  ");
        }

        System.out.println(separatorBuilder.toString().stripTrailing());
    }

    /**
     * Prints a single data row with proper formatting.
     *
     * @param record the record to display
     * @param headers the list of column headers
     * @param columnWidths the map of column widths
     * @param allColumns the set of all data columns
     */
    private static void printDataRow(DataRecord record,
                                     List<String> headers,
                                     Map<String, Integer> columnWidths,
                                     Set<String> allColumns) {
        List<String> rowValues = buildRowValues(record, allColumns);

        StringBuilder rowBuilder = new StringBuilder();
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            String value = i < rowValues.size() ? rowValues.get(i) : "-";
            int width = columnWidths.get(header);

            rowBuilder.append(String.format("%-" + width + "s", value)).append("  ");
        }

        System.out.println(rowBuilder.toString().stripTrailing());
    }

    /**
     * Builds the list of values for a single row.
     *
     * @param record the record to process
     * @param allColumns the set of all data columns
     * @return list of formatted values for the row
     */
    private static List<String> buildRowValues(DataRecord record, Set<String> allColumns) {
        List<String> rowValues = new ArrayList<>();

        // Add metadata values
        rowValues.add(String.valueOf(record.getRowNumber()));
        rowValues.add(String.valueOf(record.isValid()));
        rowValues.add(record.getErrorMessage() != null ? record.getErrorMessage() : "-");

        // Add data column values
        for (String column : allColumns) {
            Object value = record.getData() != null ? record.getData().get(column) : null;
            rowValues.add(value != null ? value.toString() : "-");
        }

        return rowValues;
    }

    /**
     * Java 11 compatible string repeat function.
     *
     * @param str the string to repeat
     * @param count the number of times to repeat
     * @return the repeated string
     */
    private static String repeatString(String str, int count) {
        if (count <= 0) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            result.append(str);
        }
        return result.toString();
    }
}
