package com.sathwick.learning.jpa.practise_2.controllers;

import com.sathwick.learning.jpa.practise_2.domain.ClinicalData;
import com.sathwick.learning.jpa.practise_2.domain.Patient2;
import com.sathwick.learning.jpa.practise_2.service.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/patients")
    public ResponseEntity<List<Patient2>> getAllPatients() {
        List<Patient2> patient2s = patientService.getAllPatients();
        return new ResponseEntity<>(patient2s, HttpStatus.OK);
    }

    @GetMapping("/patients/{id}")
    public ResponseEntity<Patient2> getPatientById(@PathVariable(name = "id") Integer id) {
        Patient2 patient2 = patientService.getPatientById(id);
        return new ResponseEntity<>(patient2, HttpStatus.OK);
    }

    @PostMapping("/patients")
    public ResponseEntity<Patient2> savePatient(@RequestBody Patient2 patient) {
        List<ClinicalData> clinicalDataList = patient.getClinicalData();
        if(Objects.nonNull(clinicalDataList)){
            clinicalDataList.forEach(clinicalData -> clinicalData.setPatient(patient));
        }
        Patient2 savedPatient2 = patientService.savePatient(patient);
        return new ResponseEntity<>(savedPatient2, HttpStatus.CREATED);
    }

    @GetMapping("/patients/analyze/{id}")
    public ResponseEntity<Patient2> analyzePatient(@PathVariable(name = "id") Integer id) {
        Patient2 patient2 = patientService.getPatientById(id);
        HashSet<String> hashSet = new HashSet<>();
        // override the current clinical data
        patient2.setClinicalData(patient2.getClinicalData()
                .stream()
                .map(clinicalData -> {
                    if(!hashSet.contains(clinicalData.getComponentName())) {
                        hashSet.add(clinicalData.getComponentName());
                        if (clinicalData.getComponentName().equals("hw")) {
                            String[] heightAndWeight = clinicalData.getComponentValue().split("/");
                            if (heightAndWeight != null && heightAndWeight.length > 1) {
                                float heightInMeters = Float.parseFloat(heightAndWeight[0]) * 0.4536F;
                                float bmi = Float.parseFloat(heightAndWeight[1]) / (heightInMeters * heightInMeters);
                                ClinicalData newClinicalData = new ClinicalData();
                                newClinicalData.setComponentName("bmi");
                                newClinicalData.setComponentValue(Float.toString(bmi));
                                newClinicalData.setMeasuredDateTime(new Timestamp(new Date().getTime()));
                                return newClinicalData;
                            }
                        }
                        return clinicalData;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
        );
        hashSet.clear();
        return new ResponseEntity<>(patient2, HttpStatus.OK);
    }
}
