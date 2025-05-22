package com.java.lld.oops.hierarchical.cache.refresh.system.test.endpoint;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SampleDataRestEndpoint {
    private final RestTemplate restTemplate;
    private final static String ENDPOINT = "https://api.restful-api.dev/objects";

    public SampleResponseListWrapper getSampleData() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        log.info("Fetching data from {}", ENDPOINT);
        ResponseEntity<List<SampleResponse>> response = restTemplate.exchange(
                ENDPOINT,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<SampleResponse>>() {}
        );
        log.info("Fetched data from {}", ENDPOINT);
        return new SampleResponseListWrapper(response.getBody());
    }
}
