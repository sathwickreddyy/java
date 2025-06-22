package com.java.lld.oops.configdriven.dataloading.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents the result of a data loading execution from a data source.
 *
 * @param dataSourceName   the name of the data source
 * @param success          true if the operation was fully or partially successful; false if completely failed
 * @param totalRecords     total number of records intended to be processed
 * @param processedRecords number of records successfully processed
 * @param errorRecords     number of records that failed to process
 * @param startTime        time when the execution started
 * @param endTime          time when the execution ended
 * @param durationMs       duration of the execution in milliseconds
 * @param errors           list of error messages encountered during processing
 */
public record ExecutionResult(String dataSourceName,
                              boolean success,
                              int totalRecords,
                              int processedRecords,
                              int errorRecords,
                              LocalDateTime startTime,
                              LocalDateTime endTime,
                              long durationMs,
                              List<String> errors,
                              LoadingStats stats) {

    /**
     * Creates an {@code ExecutionResult} representing a successful execution.
     *
     * @param dataSourceName   the name of the data source
     * @param totalRecords     total records processed
     * @param processedRecords number of records successfully processed
     * @param startTime        execution start time
     * @param endTime          execution end time
     * @return a success {@code ExecutionResult}
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
     * Creates an {@code ExecutionResult} representing a complete failure.
     *
     * @param dataSourceName the name of the data source
     * @param errors         list of error messages
     * @param startTime      execution start time
     * @param endTime        execution end time
     * @return a failure {@code ExecutionResult}
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
     * Creates an {@code ExecutionResult} representing a partial success.
     * Some records were processed successfully while others failed.
     *
     * @param dataSourceName   the name of the data source
     * @param totalRecords     total number of records intended to be processed
     * @param processedRecords number of records successfully processed
     * @param errorRecords     number of records that failed
     * @param errors           list of error messages
     * @param startTime        execution start time
     * @param endTime          execution end time
     * @return a partial success {@code ExecutionResult}
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