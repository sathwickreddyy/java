package com.sathwick.learning.jpa.practise_2.service.impl;

import com.sathwick.learning.jpa.practise_2.domain.ClinicalData;
import com.sathwick.learning.jpa.practise_2.repository.ClinicalDataRepository;
import com.sathwick.learning.jpa.practise_2.service.ClinicalDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClinicalDataServiceImpl implements ClinicalDataService {

    @Autowired
    ClinicalDataRepository repository;

    @Override
    public ClinicalData saveClinicalData(ClinicalData clinicalData) {
        return repository.save(clinicalData);
    }
}
