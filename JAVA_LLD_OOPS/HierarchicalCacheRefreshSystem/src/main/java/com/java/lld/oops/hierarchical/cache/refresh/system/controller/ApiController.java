package com.java.lld.oops.hierarchical.cache.refresh.system.controller;

import com.java.lld.oops.hierarchical.cache.refresh.system.model.SampleResponse;
import com.java.lld.oops.hierarchical.cache.refresh.system.service.SampleResponseService;
import com.java.oops.cache.types.AbstractCache;
import com.java.oops.cache.types.InMemoryCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@RestController
public class ApiController {

    @Autowired
    private SampleResponseService sampleResponseService;

    @Autowired
    private InMemoryCache<String, List<SampleResponse>> cache;

    private static final int NUMBER_OF_THREADS = 4;

    @GetMapping("/test")
    public ResponseEntity<List<List<SampleResponse>>> test() {
        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        List<List<SampleResponse>> response = new ArrayList<>();
        try {
            List<Callable<List<SampleResponse>>> tasks = new ArrayList<>();
            for (int i = 0; i < NUMBER_OF_THREADS; i++) {
                tasks.add(() -> sampleResponseService.getSampleResponse());
            }
            List<Future<List<SampleResponse>>> futureList = executorService.invokeAll(tasks);
            futureList.forEach(future -> {
                try {
                    response.add(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            log.info("Exception occurred {}", e.getMessage());
        }
        finally {
            executorService.shutdown();
        }
        log.info("Response Size {}", response.size());
        cache.getCache().keySet().forEach(key -> {
            log.info("Key {} & Value {}",key, cache.get(key));
        });
        return ResponseEntity.ok(response);
    }
}
