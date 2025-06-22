package com.java.lld.oops.configdriven.dataloading.utils;

import com.java.lld.oops.configdriven.dataloading.model.DataRecord;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Utility class for printing {@link DataRecord} instances in a tabular format.
 */
@Slf4j
public class DataRecordPrinter {

    /**
     * Prints a list of {@link DataRecord} in a tabular format.
     *
     * @param records List of processed records
     */
    public static void printRecords(List<DataRecord> records) {
        if (records == null || records.isEmpty()) {
            log.info("No records to print.");
            return;
        }

        log.info("Preparing to print {} records in tabular format...", records.size());

        // Collect all possible keys (column names)
        Set<String> allKeys = new LinkedHashSet<>();
        for (DataRecord record : records) {
            allKeys.addAll(record.data().keySet());
        }

        // Prepare column headers (row number, valid, error, then data columns)
        List<String> headers = new ArrayList<>();
        headers.add("Row");
        headers.add("Valid");
        headers.add("Error");
        headers.addAll(allKeys);

        // Determine column widths
        Map<String, Integer> columnWidths = new LinkedHashMap<>();
        for (String header : headers) {
            columnWidths.put(header, header.length());
        }

        for (DataRecord record : records) {
            columnWidths.compute("Row", (k, v) -> Math.max(v, String.valueOf(record.rowNumber()).length()));
            columnWidths.compute("Valid", (k, v) -> Math.max(v, String.valueOf(record.valid()).length()));
            columnWidths.compute("Error", (k, v) -> Math.max(v, Optional.ofNullable(record.errorMessage()).orElse("-").length()));

            for (String key : allKeys) {
                String value = Optional.ofNullable(record.data().get(key)).map(Object::toString).orElse("-");
                columnWidths.compute(key, (k, v) -> Math.max(v, value.length()));
            }
        }

        // Print header
        printRow(headers, columnWidths);
        printSeparator(columnWidths);

        // Print each record
        for (DataRecord record : records) {
            List<String> rowValues = new ArrayList<>();
            rowValues.add(String.valueOf(record.rowNumber()));
            rowValues.add(String.valueOf(record.valid()));
            rowValues.add(Optional.ofNullable(record.errorMessage()).orElse("-"));

            for (String key : allKeys) {
                Object value = record.data().getOrDefault(key, "-");
                rowValues.add(String.valueOf(value));
            }

            printRow(rowValues, columnWidths);
        }

        printSeparator(columnWidths);
        log.info("Printed {} records successfully.", records.size());
    }

    private static void printRow(List<String> values, Map<String, Integer> widths) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String header : widths.keySet()) {
            sb.append(String.format("%-" + widths.get(header) + "s", values.get(i++))).append("  ");
        }
        System.out.println(sb.toString().stripTrailing());
    }

    private static void printSeparator(Map<String, Integer> widths) {
        StringBuilder sb = new StringBuilder();
        for (int width : widths.values()) {
            sb.append("-".repeat(width)).append("  ");
        }
        System.out.println(sb.toString());
    }
}