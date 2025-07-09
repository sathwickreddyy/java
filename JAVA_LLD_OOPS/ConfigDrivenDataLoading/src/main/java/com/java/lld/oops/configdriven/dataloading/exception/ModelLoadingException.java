package com.java.lld.oops.configdriven.dataloading.exception;

/**
 * Custom runtime exception for model loading operations with enhanced error context and debugging support.
 *
 * <p>This exception is thrown when model loading operations fail due to various reasons such as
 * configuration errors, data conversion failures, validation errors, or infrastructure issues.
 * It provides a clear indication that the failure occurred during the model loading phase of
 * the data processing pipeline.</p>
 *
 * <p><b>Common Scenarios:</b></p>
 * <ul>
 *     <li><b>Configuration Errors:</b> Invalid model class names or mapping configurations</li>
 *     <li><b>Data Conversion Failures:</b> Type conversion errors during model population</li>
 *     <li><b>Validation Errors:</b> Bean validation failures on populated models</li>
 *     <li><b>Reflection Errors:</b> Field access or method invocation failures</li>
 *     <li><b>Infrastructure Errors:</b> Database connection or resource access failures</li>
 * </ul>
 *
 * <p><b>Error Handling Strategy:</b></p>
 * <ul>
 *     <li>Provides clear error messages for debugging and troubleshooting</li>
 *     <li>Preserves original exception context through cause chaining</li>
 *     <li>Enables graceful error recovery in calling code</li>
 *     <li>Supports detailed logging for operational monitoring</li>
 * </ul>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * try {
 *     ModelLoadingResult<MyModel> result = modelLoader.loadModels(MyModel.class, config);
 * } catch (ModelLoadingException e) {
 *     log.error("Model loading failed: {}", e.getMessage(), e);
 *     // Handle error appropriately
 * }
 * }</pre>
 *
 * @author sathwick
 * @since 1.0.0
 */
public class ModelLoadingException extends RuntimeException {

    /**
     * Constructs a new model loading exception with the specified detail message.
     *
     * <p>This constructor should be used when the exception is caused by a known
     * condition that can be described with a clear error message.</p>
     *
     * @param message the detail message explaining why the model loading operation failed
     */
    public ModelLoadingException(String message) {
        super(message);
    }

    /**
     * Constructs a new model loading exception with the specified detail message and cause.
     *
     * <p>This constructor should be used when the exception is caused by another exception
     * that should be preserved for debugging and error analysis purposes.</p>
     *
     * @param message the detail message explaining why the model loading operation failed
     * @param cause the underlying cause of the model loading failure
     */
    public ModelLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new model loading exception with the specified cause.
     *
     * <p>This constructor should be used when the underlying cause provides
     * sufficient context and a custom message is not necessary.</p>
     *
     * @param cause the underlying cause of the model loading failure
     */
    public ModelLoadingException(Throwable cause) {
        super(cause);
    }
}
