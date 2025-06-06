package com.java.lld.oops.hierarchical.cache.refresh.system.test.endpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${use.mock.sample.data:false}")
    private boolean useMockSampleData;
    private static final String SAMPLE_JSON = """
        [
          {
            "id": "1",
            "name": "Google Pixel 6 Pro",
            "data": {
              "color": "Cloudy White",
              "capacity": "128 GB"
            }
          },
          {
            "id": "2",
            "name": "Apple iPhone 12 Mini, 256GB, Blue",
            "data": null
          },
          {
            "id": "3",
            "name": "Apple iPhone 12 Pro Max",
            "data": {
              "color": "Cloudy White",
              "capacity GB": 512
            }
          },
          {
            "id": "4",
            "name": "Apple iPhone 11, 64GB",
            "data": {
              "price": 389.99,
              "color": "Purple"
            }
          },
          {
            "id": "5",
            "name": "Samsung Galaxy Z Fold2",
            "data": {
              "price": 689.99,
              "color": "Brown"
            }
          },
          {
            "id": "6",
            "name": "Apple AirPods",
            "data": {
              "generation": "3rd",
              "price": 120
            }
          },
          {
            "id": "7",
            "name": "Apple MacBook Pro 16",
            "data": {
              "year": 2019,
              "price": 1849.99,
              "CPU model": "Intel Core i9",
              "Hard disk size": "1 TB"
            }
          },
          {
            "id": "8",
            "name": "Apple Watch Series 8",
            "data": {
              "Strap Colour": "Elderberry",
              "Case Size": "41mm"
            }
          },
          {
            "id": "9",
            "name": "Beats Studio3 Wireless",
            "data": {
              "Color": "Red",
              "Description": "High-performance wireless noise cancelling headphones"
            }
          },
          {
            "id": "10",
            "name": "Apple iPad Mini 5th Gen",
            "data": {
              "Capacity": "64 GB",
              "Screen size": 7.9
            }
          },
          {
            "id": "11",
            "name": "Apple iPad Mini 5th Gen",
            "data": {
              "Capacity": "254 GB",
              "Screen size": 7.9
            }
          },
          {
            "id": "12",
            "name": "Apple iPad Air",
            "data": {
              "Generation": "4th",
              "Price": "419.99",
              "Capacity": "64 GB"
            }
          },
          {
            "id": "13",
            "name": "Apple iPad Air",
            "data": {
              "Generation": "4th",
              "Price": "519.99",
              "Capacity": "256 GB"
            }
          }
        ]
    """;



    public SampleResponseListWrapper getSampleData() {
        if (useMockSampleData) {
            log.info("Returning mock sample data due to flag.");
            try {
                Thread.sleep(800); // Simulate latency
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return loadMockData(); // Static JSON or inline list
        }

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

    private SampleResponseListWrapper loadMockData() {
        ObjectMapper mapper = new ObjectMapper();
        List<SampleResponse> responseList = null;
        try {
            responseList = mapper.readValue(SAMPLE_JSON, new TypeReference<List<SampleResponse>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return new SampleResponseListWrapper(responseList);
    }
}
