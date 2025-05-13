package com.java.lld.oops.hierarchical.cache.refresh.system.service;

import com.java.lld.oops.hierarchical.cache.refresh.system.model.SampleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SampleResponseService {

    private final RestTemplate restTemplate;

    public List<SampleResponse> getSampleResponse() {
        /**
         * endpoint : https://api.restful-api.dev/objects
         * returns :
         *
         *  [
         *    {
         *       "id": "1",
         *       "name": "Google Pixel 6 Pro",
         *       "data": {
         *          "color": "Cloudy White",
         *          "capacity": "128 GB"
         *       }
         *    },
         *    {
         *       "id": "2",
         *       "name": "Apple iPhone 12 Mini, 256GB, Blue",
         *       "data": null
         *    },
         *    {
         *       "id": "3",
         *       "name": "Apple iPhone 12 Pro Max",
         *       "data": {
         *          "color": "Cloudy White",
         *          "capacity GB": 512
         *       }
         *    }
         *    ]
         */

        List<SampleResponse> sampleResponseList = restTemplate.getForObject("https://api.restful-api.dev/objects", List.class);
        log.info("sampleResponseList size: {}", sampleResponseList.size());
        return sampleResponseList;
    }
}
