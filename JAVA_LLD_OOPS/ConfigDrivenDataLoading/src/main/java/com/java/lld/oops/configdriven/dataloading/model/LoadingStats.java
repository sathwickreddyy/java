package com.java.lld.oops.configdriven.dataloading.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Performance statistics for data loading operations.
 * Java 11 compatible implementation with traditional getter methods.
 *
 * @author sathwick
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoadingStats {

    /**
     * Time spent reading data from source in milliseconds
     */
    private long readTimeMs;

    /**
     * Time spent processing/transforming data in milliseconds
     */
    private long processTimeMs;

    /**
     * Time spent writing data to database in milliseconds
     */
    private long writeTimeMs;

    /**
     * Number of batches processed
     */
    private int batchCount;

    /**
     * Processing rate in records per second
     */
    private double recordsPerSecond;
}
