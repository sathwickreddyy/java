package com.java.lld.oops.hierarchical.cache.refresh.system.controller;

import com.java.lld.oops.hierarchical.cache.refresh.system.model.SampleResponse;
import com.java.lld.oops.hierarchical.cache.refresh.system.service.SampleResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ApiController {

    @Autowired
    private SampleResponseService sampleResponseService;

    @GetMapping("/test")
    public ResponseEntity<List<SampleResponse>> test() {
        return ResponseEntity.ok(sampleResponseService.getSampleResponse());
    }
}
