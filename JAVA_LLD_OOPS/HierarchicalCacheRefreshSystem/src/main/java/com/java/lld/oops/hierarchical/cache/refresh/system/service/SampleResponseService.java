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
import java.util.Optional;

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
        leaderElectionService.electLeader();
        if(leaderElectionService.isLeader()) {
            log.info("I'm Leader, updating distributed cache");
            distributedCache.put(uniqueId, new SampleResponseListWrapper(sampleResponseList));
        }
        else {
            log.info("Not leader, skipping cache update");
        }
        return sampleResponseList;
    }

    public Optional<List<SampleResponse>> getSampleResponseFromCache() {
        return inMemoryCache.get(SystemIdentifierUtil.getSystemUniqueKey());
    }

    public List<SampleResponse> getSampleResponseWithoutLeaderElection() {
        List<SampleResponse> sampleResponseList = restTemplate.getForObject("https://api.restful-api.dev/objects", List.class);
        log.info("sampleResponseList size: {}", sampleResponseList.size());
        String uniqueId = SystemIdentifierUtil.getSystemUniqueKey();
        synchronized (this) {
            log.info("Updating caches");
            inMemoryCache.put(uniqueId, sampleResponseList);
            distributedCache.put(uniqueId, new SampleResponseListWrapper(sampleResponseList));
        }
        return sampleResponseList;
    }
}
