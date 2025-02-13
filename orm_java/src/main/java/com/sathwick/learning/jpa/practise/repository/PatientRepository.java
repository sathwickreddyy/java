package com.sathwick.learning.jpa.practise.repository;

import com.sathwick.learning.jpa.practise.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {
}
