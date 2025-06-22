package com.java.lld.oops.configdriven.dataloading.factory;

import com.java.lld.oops.configdriven.dataloading.loader.DataLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory responsible for managing and retrieving {@link DataLoader} implementations.
 * <pre>
 * This class is automatically registered as a Spring bean and collects all available
 * {@code DataLoader} beans. It provides a lookup mechanism to retrieve the correct loader
 * by its type (e.g., "CSV", "JSON").
 * </pre>
 * Compatible with Java 11, 17, and 21.
 *
 * @author sathwick
 */
@Slf4j
@Component
public class DataSourcingFactory {

    private final Map<String, DataLoader> loaders;

    /**
     * Constructs the factory and initializes the map of supported data loaders.
     *
     * @param dataLoaders list of all available {@link DataLoader} implementations injected by Spring
     */
    public DataSourcingFactory(List<DataLoader> dataLoaders) {
        this.loaders = dataLoaders.stream()
                .collect(Collectors.toMap(
                        DataLoader::getType,
                        Function.identity(),
                        (existing, replacement) -> {
                            log.warn("Duplicate DataLoader type found: {}. Overwriting previous instance.", existing.getType());
                            return replacement;
                        }
                ));
        log.info("Initialized DataSourcingFactory with supported types: {}", loaders.keySet());
    }

    /**
     * Retrieves a {@link DataLoader} by its type.
     *
     * @param type the type of data source (case-sensitive)
     * @return the corresponding {@link DataLoader}
     * @throws IllegalArgumentException if no loader exists for the given type
     */
    public DataLoader getLoader(String type) {
        if (type == null || type.isBlank()) {
            log.error("Attempted to get loader with null or blank type");
            throw new IllegalArgumentException("Loader type must not be null or blank");
        }

        DataLoader loader = loaders.get(type);
        if (loader == null) {
            log.error("No DataLoader found for type: {}", type);
            throw new IllegalArgumentException("No DataLoader found for type: " + type);
        }

        log.debug("Retrieved DataLoader for type: {}", type);
        return loader;
    }

    /**
     * Returns a list of all supported loader types.
     *
     * @return a list of supported types (e.g., ["CSV", "JSON"])
     */
    public List<String> getSupportedTypes() {
        return List.copyOf(loaders.keySet());
    }
}