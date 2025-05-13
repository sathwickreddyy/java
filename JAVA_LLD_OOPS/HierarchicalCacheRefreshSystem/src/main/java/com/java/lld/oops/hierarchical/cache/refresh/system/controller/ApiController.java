package com.java.lld.oops.hierarchical.cache.refresh.system.controller;

import com.java.lld.oops.hierarchical.cache.refresh.system.model.SampleResponse;
import com.java.lld.oops.hierarchical.cache.refresh.system.service.SampleResponseService;
import com.java.oops.cache.types.InMemoryCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
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

    @GetMapping("/testDistributedCache")
    public ResponseEntity<List<SampleResponse>> test2() {
//        Optional<List<SampleResponse>> sampleResponseList = sampleResponseService.getSampleResponseFromCache();
//        return sampleResponseList.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.ok(sampleResponseService.getSampleResponse()));
        return ResponseEntity.ok(sampleResponseService.getSampleResponse());
    }

    @GetMapping("/testDistributedCacheWithoutLeaderElection")
    public ResponseEntity<List<SampleResponse>> test3() {
        return ResponseEntity.ok(sampleResponseService.getSampleResponseWithoutLeaderElection());
    }
}

/*
Testing in docker

import requests
from concurrent.futures import ThreadPoolExecutor

urls = [
    "http://localhost:8081/testDistributedCache",
    "http://localhost:8082/testDistributedCache",
    "http://localhost:8083/testDistributedCache",
    "http://localhost:8084/testDistributedCache"
]

urls_without_locking = [
    "http://localhost:8081/testDistributedCacheWithoutLeaderElection",
    "http://localhost:8082/testDistributedCacheWithoutLeaderElection",
    "http://localhost:8083/testDistributedCacheWithoutLeaderElection",
    "http://localhost:8084/testDistributedCacheWithoutLeaderElection"
]

def call_api(url):
    try:
        response = requests.get(url)
        print(f"{url} -> {response.status_code}")
    except Exception as e:
        print(f"{url} -> Error: {str(e)}")

with ThreadPoolExecutor(max_workers=4) as executor:
    executor.map(call_api, urls_without_locking)


 */