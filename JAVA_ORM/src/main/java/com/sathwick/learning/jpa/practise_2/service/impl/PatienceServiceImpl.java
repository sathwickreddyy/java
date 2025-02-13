package com.sathwick.learning.jpa.practise_2.service.impl;

import com.sathwick.learning.jpa.practise_2.domain.Patient2;
import com.sathwick.learning.jpa.practise_2.repository.PatientRepo;
import com.sathwick.learning.jpa.practise_2.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatienceServiceImpl implements PatientService {
    @Autowired
    PatientRepo repository;

    @Override
    public List<Patient2> getAllPatients() {
        return repository.findAll();
    }

    @Override
    public Patient2 getPatientById(Integer id) {
        Optional<Patient2> patient = repository.findById(id);
        return patient.orElse(null);
    }

    @Override
    public Patient2 savePatient(Patient2 patient2) {
        return repository.save(patient2);
    }
}
