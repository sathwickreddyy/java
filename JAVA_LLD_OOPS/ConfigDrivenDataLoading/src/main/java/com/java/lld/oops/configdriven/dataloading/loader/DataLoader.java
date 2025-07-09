package com.java.lld.oops.configdriven.dataloading.loader;

import com.java.lld.oops.configdriven.dataloading.config.DataLoaderConfiguration;
import com.java.lld.oops.configdriven.dataloading.model.DataRecord;

import java.util.stream.Stream;

/**
 * Core interface for all data loading implementations in the config-driven data loading framework.
 *
 * <p>This interface defines the contract that all data loaders must implement to participate
 * in the framework's data loading pipeline. It provides a unified abstraction over different
 * data source types while maintaining type safety and performance characteristics.</p>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *     <li><b>Type Safety:</b> Strongly typed interface with clear contracts</li>
 *     <li><b>Performance:</b> Stream-based processing for memory efficiency</li>
 *     <li><b>Extensibility:</b> Easy to implement for new data source types</li>
 *     <li><b>Configuration-Driven:</b> Behavior controlled by external configuration</li>
 * </ul>
 *
 * <p><b>Implementation Requirements:</b></p>
 * <ul>
 *     <li><b>Thread Safety:</b> Implementations should be thread-safe for concurrent use</li>
 *     <li><b>Resource Management:</b> Proper cleanup of resources (files, connections, etc.)</li>
 *     <li><b>Error Handling:</b> Graceful handling of errors with meaningful messages</li>
 *     <li><b>Streaming:</b> Return lazy-evaluated streams for memory efficiency</li>
 * </ul>
 *
 * <p><b>Supported Data Source Types:</b></p>
 * <ul>
 *     <li><b>CSV:</b> Comma-separated values files</li>
 *     <li><b>Excel:</b> Microsoft Excel spreadsheets</li>
 *     <li><b>JSON:</b> JSON files and API responses</li>
 *     <li><b>API:</b> REST API endpoints</li>
 *     <li><b>Database:</b> SQL queries and stored procedures</li>
 * </ul>
 *
 * <p><b>Java 11 Compatibility:</b></p>
 * <ul>
 *     <li>Uses traditional getter methods in configuration access</li>
 *     <li>Compatible with Java 11, 17, and 21</li>
 *     <li>No behavioral differences across Java versions</li>
 * </ul>
 *
 * <p><b>Example Implementation:</b></p>
 * <pre>{@code
 * @Component
 * public class CustomDataLoader implements DataLoader {
 *
 *     @Override
 *     public String getType() {
 *         return "CUSTOM";
 *     }
 *
 *     @Override
 *     public Stream<DataRecord> loadData(DataSourceDefinition config) {
 *         // Implementation specific logic
 *         return dataStream;
 *     }
 * }
 * }</pre>
 *
 * @author sathwick
 * @since 1.0.0
 */
public interface DataLoader {

    /**
     * Returns the unique type identifier for this data loader implementation.
     *
     * <p>This identifier is used by the data source factory to match configuration
     * types with appropriate loader implementations. The type should be:</p>
     * <ul>
     *     <li><b>Unique:</b> No two loaders should return the same type</li>
     *     <li><b>Descriptive:</b> Clearly indicates the data source type</li>
     *     <li><b>Uppercase:</b> Convention for consistency across implementations</li>
     *     <li><b>Stable:</b> Should not change between versions</li>
     * </ul>
     *
     * <p><b>Standard Types:</b></p>
     * <ul>
     *     <li>"CSV" - for CSV file processing</li>
     *     <li>"EXCEL" - for Excel file processing</li>
     *     <li>"JSON" - for JSON file and API processing</li>
     *     <li>"API" - for REST API processing</li>
     *     <li>"DATABASE" - for database query processing</li>
     * </ul>
     *
     * @return the type identifier string (e.g., "CSV", "EXCEL", "JSON", "API")
     */
    String getType();

    /**
     * Loads data from the configured source and returns a stream of DataRecord objects.
     *
     * <p>This method is the core of the data loading process and must:</p>
     * <ul>
     *     <li><b>Validate Configuration:</b> Ensure all required parameters are present</li>
     *     <li><b>Connect to Source:</b> Establish connection to the data source</li>
     *     <li><b>Stream Data:</b> Return a lazy-evaluated stream for memory efficiency</li>
     *     <li><b>Handle Errors:</b> Convert errors to invalid DataRecord objects when possible</li>
     *     <li><b>Manage Resources:</b> Ensure proper cleanup of connections and files</li>
     * </ul>
     *
     * <p><b>Stream Characteristics:</b></p>
     * <ul>
     *     <li><b>Lazy Evaluation:</b> Data should be loaded only when consumed</li>
     *     <li><b>Sequential Processing:</b> Records should be processed in source order</li>
     *     <li><b>Resource Cleanup:</b> Use onClose() handlers for resource management</li>
     *     <li><b>Error Resilience:</b> Individual record errors shouldn't break the stream</li>
     * </ul>
     *
     * <p><b>Error Handling Strategy:</b></p>
     * <ul>
     *     <li><b>Configuration Errors:</b> Throw IllegalArgumentException with clear messages</li>
     *     <li><b>Connection Errors:</b> Throw RuntimeException with context information</li>
     *     <li><b>Data Errors:</b> Return invalid DataRecord objects with error details</li>
     *     <li><b>Resource Errors:</b> Log warnings but don't fail the operation</li>
     * </ul>
     *
     * <p><b>Performance Considerations:</b></p>
     * <ul>
     *     <li>Use streaming to handle datasets larger than available memory</li>
     *     <li>Minimize object creation during record processing</li>
     *     <li>Implement proper buffering for I/O operations</li>
     *     <li>Consider parallel processing for CPU-intensive operations</li>
     * </ul>
     *
     * @param config the data source configuration containing connection parameters,
     *               file paths, authentication details, and other source-specific settings
     * @return a stream of {@link DataRecord} objects representing the loaded data,
     *         where each record is either valid (containing data) or invalid (containing error information)
     * @throws IllegalArgumentException if the configuration is invalid or missing required parameters
     * @throws RuntimeException if connection to the data source fails or other unrecoverable errors occur
     */
    Stream<DataRecord> loadData(DataLoaderConfiguration.DataSourceDefinition config);
}
