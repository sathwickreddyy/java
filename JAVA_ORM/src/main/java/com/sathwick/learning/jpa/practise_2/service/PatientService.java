package com.sathwick.learning.jpa.practise_2.service;

import com.sathwick.learning.jpa.practise_2.domain.Patient2;

import java.util.List;

public interface PatientService {
    List<Patient2> getAllPatients();

    Patient2 getPatientById(Integer id);

    Patient2 savePatient(Patient2 patient2);
}
