package com.sathwick.learning.jpa.practise_2.controllers;

import com.sathwick.learning.jpa.practise_2.domain.ClinicalData;
import com.sathwick.learning.jpa.practise_2.domain.Patient2;
import com.sathwick.learning.jpa.practise_2.domain.dto.ClinicalDataRequest;
import com.sathwick.learning.jpa.practise_2.service.ClinicalDataService;
import com.sathwick.learning.jpa.practise_2.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ClinicalDataController {
    @Autowired
    ClinicalDataService clinicalDataService;
    @Autowired
    PatientService patienceService;

    @PostMapping("/clinicaldata")
    public ResponseEntity<ClinicalData> saveClinicalData(@RequestBody ClinicalDataRequest request) {
        Patient2 patient2 = patienceService.getPatientById(request.getPatientId());
        ClinicalData data = clinicalDataService.saveClinicalData(request.toClinicalData(patient2));
        return new ResponseEntity<>(data, HttpStatus.CREATED);
    }
}
