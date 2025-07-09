package com.java.lld.oops.configdriven.dataloading.loader;

import com.java.lld.oops.configdriven.dataloading.config.DataLoaderConfiguration;
import com.java.lld.oops.configdriven.dataloading.model.DataRecord;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Advanced JSON data loader with comprehensive nested path support and flexible mapping capabilities.
 *
 * <p>This loader provides sophisticated JSON processing capabilities including:</p>
 * <ul>
 *     <li><b>Nested Path Extraction:</b> Support for complex JSON path expressions</li>
 *     <li><b>JSONPath Integration:</b> Full JSONPath specification support</li>
 *     <li><b>Flexible Mapping:</b> Both model and table target support</li>
 *     <li><b>Error Resilience:</b> Graceful handling of missing paths and malformed JSON</li>
 *     <li><b>Performance Optimization:</b> Streaming processing for large JSON files</li>
 * </ul>
 *
 * <p><b>Supported JSON Path Formats:</b></p>
 * <ul>
 *     <li><b>Simple Fields:</b> "name", "id", "status"</li>
 *     <li><b>Nested Objects:</b> "user.profile.name", "order.customer.address.city"</li>
 *     <li><b>Array Access:</b> "items[0].name", "users[*].email"</li>
 *     <li><b>JSONPath Expressions:</b> "$.data.users[?(@.active == true)].name"</li>
 *     <li><b>Conditional Paths:</b> "$.products[?(@.price > 100)].title"</li>
 * </ul>
 *
 * <p><b>Java 11 Compatibility:</b></p>
 * <ul>
 *     <li>Uses traditional getter methods instead of record accessors</li>
 *     <li>Compatible with Java 11, 17, and 21</li>
 *     <li>No behavioral differences across Java versions</li>
 * </ul>
 *
 * @author sathwick
 * @since 1.0.0
 */
@Slf4j
@Component
public class JsonDataLoader implements DataLoader {

    private final ObjectMapper objectMapper;

    /**
     * Constructs a new JsonDataLoader with optimized ObjectMapper configuration.
     */
    public JsonDataLoader() {
        this.objectMapper = new ObjectMapper();
        // Configure ObjectMapper for optimal JSON processing
        this.objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS, true);
        this.objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

        log.debug("JsonDataLoader initialized with optimized ObjectMapper configuration");
    }

    @Override
    public String getType() {
        return "JSON";
    }

    /**
     * Loads JSON data with comprehensive nested path support and flexible target mapping.
     *
     * <p>This method orchestrates the complete JSON loading process:</p>
     * <ol>
     *     <li><b>File Validation:</b> Validates JSON file existence and readability</li>
     *     <li><b>JSON Parsing:</b> Parses JSON content with error handling</li>
     *     <li><b>Path Extraction:</b> Extracts data using JSONPath or direct access</li>
     *     <li><b>Record Conversion:</b> Converts JSON nodes to DataRecord objects</li>
     *     <li><b>Nested Mapping:</b> Handles complex nested field mapping</li>
     * </ol>
     *
     * @param config the data source configuration containing JSON file path and mapping rules
     * @return stream of DataRecord objects with nested field support
     */
    @Override
    public Stream<DataRecord> loadData(DataLoaderConfiguration.DataSourceDefinition config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }

        var source = config.getSource();
        String filePath = source.getFilePath();

        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON file path cannot be null or empty");
        }

        log.info("Loading JSON file from path: {}", filePath);

        try {
            // Validate and load JSON file
            File jsonFile = validateJsonFile(filePath);
            JsonNode rootNode = objectMapper.readTree(jsonFile);

            // Extract data nodes using JSONPath or direct access
            List<JsonNode> dataNodes = extractDataNodes(rootNode, source.getJsonPath());

            // Convert to DataRecord stream with nested field support
            return convertToDataRecords(dataNodes, config);

        } catch (IOException e) {
            log.error("I/O error while reading JSON file: {}", filePath, e);
            throw new RuntimeException("I/O error while reading JSON file: " + filePath, e);
        } catch (Exception e) {
            log.error("Unexpected error while loading JSON data from file: {}", filePath, e);
            throw new RuntimeException("Unexpected error while loading JSON data from: " + filePath, e);
        }
    }

    /**
     * Validates JSON file existence and readability.
     *
     * @param filePath the path to the JSON file
     * @return validated File object
     * @throws IllegalArgumentException if file doesn't exist or isn't readable
     */
    private File validateJsonFile(String filePath) {
        File file = new File(filePath.strip());

        if (!file.exists()) {
            log.error("JSON file not found: {}", filePath);
            throw new IllegalArgumentException("JSON file not found: " + filePath);
        }

        if (!file.isFile()) {
            log.error("Path does not point to a file: {}", filePath);
            throw new IllegalArgumentException("Path is not a file: " + filePath);
        }

        if (!file.canRead()) {
            log.error("JSON file is not readable: {}", filePath);
            throw new IllegalArgumentException("JSON file is not readable: " + filePath);
        }

        log.debug("JSON file validation passed for: {} (size: {} bytes)", filePath, file.length());
        return file;
    }

    /**
     * Extracts data nodes from JSON using JSONPath expressions or direct access.
     *
     * @param rootNode the root JSON node
     * @param jsonPath the JSONPath expression (optional)
     * @return list of extracted JsonNode objects
     */
    private List<JsonNode> extractDataNodes(JsonNode rootNode, String jsonPath) {
        List<JsonNode> dataNodes = new ArrayList<>();

        if (jsonPath != null && !jsonPath.trim().isEmpty()) {
            // Use JSONPath extraction
            dataNodes = extractWithJsonPath(rootNode, jsonPath.trim());
            log.debug("Extracted {} nodes using JSONPath: {}", dataNodes.size(), jsonPath);
        } else {
            // Direct extraction without JSONPath
            dataNodes = extractDirectly(rootNode);
            log.debug("Extracted {} nodes using direct access", dataNodes.size());
        }

        return dataNodes;
    }

    /**
     * Extracts data using JSONPath expressions with comprehensive error handling.
     *
     * @param rootNode the root JSON node
     * @param jsonPath the JSONPath expression
     * @return list of extracted JsonNode objects
     */
    private List<JsonNode> extractWithJsonPath(JsonNode rootNode, String jsonPath) {
        List<JsonNode> nodes = new ArrayList<>();

        try {
            String jsonString = rootNode.toString();

            // Handle different JSONPath result types
            Object result = JsonPath.read(jsonString, jsonPath);

            if (result instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> resultList = (List<Object>) result;

                for (Object item : resultList) {
                    JsonNode node = objectMapper.valueToTree(item);
                    nodes.add(node);
                }
            } else {
                // Single result
                JsonNode node = objectMapper.valueToTree(result);
                nodes.add(node);
            }

            log.debug("JSONPath extraction successful: {} -> {} nodes", jsonPath, nodes.size());

        } catch (PathNotFoundException e) {
            log.warn("JSONPath not found: {} in JSON structure", jsonPath);
            // Return empty list for missing paths
        } catch (Exception e) {
            log.error("JSONPath extraction failed for path '{}': {}", jsonPath, e.getMessage(), e);
            throw new RuntimeException("JSONPath extraction failed: " + jsonPath, e);
        }

        return nodes;
    }

    /**
     * Extracts data directly from JSON without JSONPath.
     *
     * @param rootNode the root JSON node
     * @return list of JsonNode objects
     */
    private List<JsonNode> extractDirectly(JsonNode rootNode) {
        List<JsonNode> dataNodes = new ArrayList<>();

        if (rootNode.isArray()) {
            // Root is an array
            rootNode.forEach(dataNodes::add);
            log.debug("Root node is array with {} elements", dataNodes.size());
        } else if (rootNode.isObject()) {
            // Root is an object, look for common data array fields
            String[] commonDataFields = {"data", "items", "results", "records", "content", "list"};

            boolean foundDataArray = false;
            for (String field : commonDataFields) {
                JsonNode fieldNode = rootNode.get(field);
                if (fieldNode != null && fieldNode.isArray()) {
                    fieldNode.forEach(dataNodes::add);
                    log.debug("Found data array in field '{}' with {} elements", field, dataNodes.size());
                    foundDataArray = true;
                    break;
                }
            }

            // If no array found, treat the object itself as a single record
            if (!foundDataArray) {
                dataNodes.add(rootNode);
                log.debug("Treating root object as single record");
            }
        } else {
            // Root is a primitive value
            dataNodes.add(rootNode);
            log.debug("Root node is primitive value");
        }

        return dataNodes;
    }

    /**
     * Converts JsonNode list to DataRecord stream with nested field mapping support.
     *
     * @param dataNodes list of JsonNode objects
     * @param config data source configuration
     * @return stream of DataRecord objects
     */
    private Stream<DataRecord> convertToDataRecords(List<JsonNode> dataNodes,
                                                    DataLoaderConfiguration.DataSourceDefinition config) {
        AtomicInteger rowCounter = new AtomicInteger(0);

        return dataNodes.stream()
                .map(node -> convertJsonNodeToDataRecord(node, rowCounter.incrementAndGet(), config))
                .filter(Objects::nonNull);
    }

    /**
     * Converts a single JsonNode to DataRecord with comprehensive nested field mapping.
     *
     * @param node JsonNode to convert
     * @param rowNumber row number for tracking
     * @param config data source configuration
     * @return DataRecord object or null if conversion fails
     */
    private DataRecord convertJsonNodeToDataRecord(JsonNode node, int rowNumber,
                                                   DataLoaderConfiguration.DataSourceDefinition config) {
        try {
            Map<String, Object> data = new HashMap<>();

            // Process column mappings with nested path support
            if (config.getColumnMapping() != null && !config.getColumnMapping().isEmpty()) {
                // Use explicit column mappings with nested path support
                processColumnMappings(node, data, config.getColumnMapping(), rowNumber);
            } else {
                // Extract all fields directly
                extractAllFields(node, data);
            }

            log.trace("Converted JSON node to DataRecord for row {}: {} fields", rowNumber, data.size());
            return DataRecord.valid(data, rowNumber);

        } catch (Exception e) {
            log.error("Failed to convert JSON node to DataRecord for row {}: {}", rowNumber, e.getMessage(), e);
            return DataRecord.invalid(Map.of(), rowNumber,
                    String.format("JSON conversion error: %s", e.getMessage()));
        }
    }

    /**
     * Processes column mappings with comprehensive nested path support.
     * FIXED: Now stores values using TARGET field names, not source paths
     */
    private void processColumnMappings(JsonNode node, Map<String, Object> data,
                                       List<DataLoaderConfiguration.ColumnMapping> mappings,
                                       int rowNumber) {
        for (DataLoaderConfiguration.ColumnMapping mapping : mappings) {
            String sourcePath = mapping.getSource();        // e.g., "profile.personal.firstName"
            String targetField = mapping.getTarget();       // e.g., "firstName"

            try {
                Object value = extractValueFromPath(node, sourcePath);

                if (value != null) {
                    // FIXED: Store using TARGET field name, not source path
                    data.put(targetField, value);  // Use targetField instead of sourcePath
                    log.trace("Row {}: Mapped '{}' -> '{}' with value: {}",
                            rowNumber, sourcePath, targetField, value);
                } else if (mapping.getDefaultValue() != null) {
                    // FIXED: Store default using TARGET field name
                    data.put(targetField, mapping.getDefaultValue());
                    log.trace("Row {}: Applied default value for '{}' -> '{}'",
                            rowNumber, sourcePath, targetField);
                }

            } catch (Exception e) {
                log.warn("Row {}: Failed to extract value from path '{}': {}",
                        rowNumber, sourcePath, e.getMessage());

                if (mapping.getDefaultValue() != null) {
                    // FIXED: Store default using TARGET field name
                    data.put(targetField, mapping.getDefaultValue());
                    log.trace("Row {}: Applied default value after extraction failure for '{}'",
                            rowNumber, targetField);
                }
            }
        }
    }


    /**
     * Extracts value from JSON node using nested path expressions.
     *
     * <p>Supports various path formats:</p>
     * <ul>
     *     <li><b>Simple:</b> "name" -> node.get("name")</li>
     *     <li><b>Nested:</b> "user.profile.name" -> node.get("user").get("profile").get("name")</li>
     *     <li><b>Array:</b> "items[0].name" -> node.get("items").get(0).get("name")</li>
     *     <li><b>JSONPath:</b> "$.user.addresses[?(@.type=='home')].city"</li>
     * </ul>
     *
     * @param node the JSON node to extract from
     * @param path the path expression
     * @return extracted value or null if not found
     */
    private Object extractValueFromPath(JsonNode node, String path) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }

        String trimmedPath = path.trim();

        // Handle JSONPath expressions (starting with $ or containing advanced operators)
        if (trimmedPath.startsWith("$") || trimmedPath.contains("[?") || trimmedPath.contains("*")) {
            return extractWithJsonPathExpression(node, trimmedPath);
        }

        // Handle simple dot notation and array access
        return extractWithDotNotation(node, trimmedPath);
    }

    /**
     * Extracts value using JSONPath expressions.
     *
     * @param node the JSON node
     * @param jsonPath the JSONPath expression
     * @return extracted value or null
     */
    private Object extractWithJsonPathExpression(JsonNode node, String jsonPath) {
        try {
            String jsonString = node.toString();
            Object result = JsonPath.read(jsonString, jsonPath);

            if (result instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> resultList = (List<Object>) result;
                return resultList.isEmpty() ? null : resultList.get(0);
            }

            return result;

        } catch (PathNotFoundException e) {
            log.trace("JSONPath not found: {}", jsonPath);
            return null;
        } catch (Exception e) {
            log.warn("JSONPath extraction failed for '{}': {}", jsonPath, e.getMessage());
            return null;
        }
    }

    /**
     * Extracts value using dot notation and array access.
     *
     * @param node the JSON node
     * @param path the dot notation path
     * @return extracted value or null
     */
    private Object extractWithDotNotation(JsonNode node, String path) {
        JsonNode currentNode = node;
        String[] pathParts = path.split("\\.");

        for (String part : pathParts) {
            if (currentNode == null) {
                return null;
            }

            // Handle array access like "items[0]"
            if (part.contains("[") && part.contains("]")) {
                currentNode = handleArrayAccess(currentNode, part);
            } else {
                // Simple field access
                currentNode = currentNode.get(part);
            }
        }

        return currentNode != null ? convertJsonNodeValue(currentNode) : null;
    }

    /**
     * Handles array access in path expressions.
     *
     * @param node the current JSON node
     * @param part the path part with array access
     * @return the accessed node or null
     */
    private JsonNode handleArrayAccess(JsonNode node, String part) {
        int bracketStart = part.indexOf('[');
        int bracketEnd = part.indexOf(']');

        if (bracketStart == -1 || bracketEnd == -1 || bracketEnd <= bracketStart) {
            log.warn("Invalid array access syntax: {}", part);
            return null;
        }

        String fieldName = part.substring(0, bracketStart);
        String indexStr = part.substring(bracketStart + 1, bracketEnd);

        JsonNode arrayNode = node.get(fieldName);
        if (arrayNode == null || !arrayNode.isArray()) {
            return null;
        }

        try {
            if ("*".equals(indexStr)) {
                // Return first element for wildcard access
                return !arrayNode.isEmpty() ? arrayNode.get(0) : null;
            } else {
                int index = Integer.parseInt(indexStr);
                return arrayNode.get(index);
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid array index: {}", indexStr);
            return null;
        }
    }

    /**
     * Extracts all fields from JSON node without specific mappings.
     *
     * @param node the JSON node
     * @param data the target data map
     */
    private void extractAllFields(JsonNode node, Map<String, Object> data) {
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode fieldValue = entry.getValue();
                Object convertedValue = convertJsonNodeValue(fieldValue);
                data.put(fieldName, convertedValue);
            });
        } else {
            // For non-object nodes, use a default field name
            data.put("value", convertJsonNodeValue(node));
        }
    }

    /**
     * Converts JsonNode value to appropriate Java object.
     *
     * @param node JsonNode to convert
     * @return converted Java object
     */
    private Object convertJsonNodeValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        } else if (node.isBoolean()) {
            return node.booleanValue();
        } else if (node.isInt()) {
            return node.intValue();
        } else if (node.isLong()) {
            return node.longValue();
        } else if (node.isDouble()) {
            return node.doubleValue();
        } else if (node.isTextual()) {
            return node.textValue();
        } else if (node.isArray() || node.isObject()) {
            return node.toString(); // Convert complex types to JSON string
        } else {
            return node.asText();
        }
    }
}
