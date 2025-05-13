package com.java.lld.oops.hierarchical.cache.refresh.system.service;

import com.java.lld.oops.hierarchical.cache.refresh.system.leaderelection.LeaderElectionService;
import com.java.lld.oops.hierarchical.cache.refresh.system.model.SampleResponse;
import com.java.lld.oops.hierarchical.cache.refresh.system.model.SampleResponseListWrapper;
import com.java.lld.oops.hierarchical.cache.refresh.system.utils.SystemIdentifierUtil;
import com.java.oops.cache.types.InMemoryCache;
import com.java.oops.cache.types.RedisDistributedCache;
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
    private final RedisDistributedCache<String, SampleResponseListWrapper> distributedCache;
    private final InMemoryCache<String, List<SampleResponse>> inMemoryCache;
    private final LeaderElectionService leaderElectionService;

    public List<SampleResponse> getSampleResponse() {
        List<SampleResponse> sampleResponseList = restTemplate.getForObject("https://api.restful-api.dev/objects", List.class);
        log.info("sampleResponseList size: {}", sampleResponseList.size());
        String uniqueId = SystemIdentifierUtil.getSystemUniqueKey();
        synchronized (this) {
            log.info("Updating in memory cache");
            inMemoryCache.put(uniqueId, sampleResponseList);
        }
        try {
            leaderElectionService.electLeader();
            if(leaderElectionService.isLeader()) {
                try {
                    log.info("Leader, updating distributed cache");
                    distributedCache.put(uniqueId, new SampleResponseListWrapper(sampleResponseList));
                }
                finally {
                    leaderElectionService.cleanUp();
                }
            }
            else {
                log.info("Not leader, skipping cache update");
            }
        }
        finally {
            leaderElectionService.cleanUp();
        }
        return sampleResponseList;
    }
}
