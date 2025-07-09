package com.java.lld.oops.configdriven.dataloading.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * Represents a single data record with its validation status and error information.
 * Java 11 compatible implementation using traditional class structure.
 *
 * @author sathwick
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataRecord {

    /**
     * The actual data content as key-value pairs
     */
    private Map<String, Object> data;

    /**
     * The row number for tracking and debugging purposes
     */
    private int rowNumber;

    /**
     * Flag indicating whether this record is valid
     */
    private boolean valid;

    /**
     * Error message if the record is invalid, null otherwise
     */
    private String errorMessage;

    /**
     * Creates a valid DataRecord instance
     *
     * @param data the record data
     * @param rowNumber the row number
     * @return a valid DataRecord
     */
    public static DataRecord valid(Map<String, Object> data, int rowNumber) {
        return new DataRecord(data, rowNumber, true, null);
    }

    /**
     * Creates an invalid DataRecord instance with error message
     *
     * @param data the record data (may be empty)
     * @param rowNumber the row number
     * @param errorMessage the error description
     * @return an invalid DataRecord
     */
    public static DataRecord invalid(Map<String, Object> data, int rowNumber, String errorMessage) {
        return new DataRecord(data, rowNumber, false, errorMessage);
    }
}
