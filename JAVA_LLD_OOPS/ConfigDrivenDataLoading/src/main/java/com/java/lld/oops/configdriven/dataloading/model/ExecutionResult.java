package com.java.lld.oops.configdriven.dataloading.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents the result of a data loading execution from a data source.
 * Java 11 compatible implementation with traditional getter methods.
 *
 * @author sathwick
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResult {

    /**
     * The name of the data source that was executed
     */
    private String dataSourceName;

    /**
     * True if the operation was fully or partially successful; false if completely failed
     */
    private boolean success;

    /**
     * Total number of records intended to be processed
     */
    private int totalRecords;

    /**
     * Number of records successfully processed
     */
    private int processedRecords;

    /**
     * Number of records that failed to process
     */
    private int errorRecords;

    /**
     * Time when the execution started
     */
    private LocalDateTime startTime;

    /**
     * Time when the execution ended
     */
    private LocalDateTime endTime;

    /**
     * Duration of the execution in milliseconds
     */
    private long durationMs;

    /**
     * List of error messages encountered during processing
     */
    private List<String> errors;

    /**
     * Loading statistics for performance analysis
     */
    private LoadingStats stats;

    /**
     * Creates an ExecutionResult representing a successful execution
     *
     * @param dataSourceName the name of the data source
     * @param totalRecords total records processed
     * @param processedRecords number of records successfully processed
     * @param startTime execution start time
     * @param endTime execution end time
     * @param stats loading statistics
     * @return a success ExecutionResult
     */
    public static ExecutionResult success(String dataSourceName,
                                          int totalRecords,
                                          int processedRecords,
                                          LocalDateTime startTime,
                                          LocalDateTime endTime,
                                          LoadingStats stats) {
        return new ExecutionResult(
                dataSourceName,
                true,
                totalRecords,
                processedRecords,
                0,
                startTime,
                endTime,
                Duration.between(startTime, endTime).toMillis(),
                List.of(),
                stats
        );
    }

    /**
     * Creates an ExecutionResult representing a complete failure
     *
     * @param dataSourceName the name of the data source
     * @param errors list of error messages
     * @param startTime execution start time
     * @param endTime execution end time
     * @return a failure ExecutionResult
     */
    public static ExecutionResult failure(String dataSourceName,
                                          List<String> errors,
                                          LocalDateTime startTime,
                                          LocalDateTime endTime) {
        return new ExecutionResult(
                dataSourceName,
                false,
                0,
                0,
                0,
                startTime,
                endTime,
                Duration.between(startTime, endTime).toMillis(),
                errors,
                null
        );
    }

    /**
     * Creates an ExecutionResult representing a partial success
     * Some records were processed successfully while others failed
     *
     * @param dataSourceName the name of the data source
     * @param totalRecords total number of records intended to be processed
     * @param processedRecords number of records successfully processed
     * @param errorRecords number of records that failed
     * @param errors list of error messages
     * @param startTime execution start time
     * @param endTime execution end time
     * @param stats loading statistics
     * @return a partial success ExecutionResult
     */
    public static ExecutionResult partialSuccess(String dataSourceName,
                                                 int totalRecords,
                                                 int processedRecords,
                                                 int errorRecords,
                                                 List<String> errors,
                                                 LocalDateTime startTime,
                                                 LocalDateTime endTime,
                                                 LoadingStats stats) {
        return new ExecutionResult(
                dataSourceName,
                processedRecords > 0,
                totalRecords,
                processedRecords,
                errorRecords,
                startTime,
                endTime,
                Duration.between(startTime, endTime).toMillis(),
                errors,
                stats
        );
    }
}
