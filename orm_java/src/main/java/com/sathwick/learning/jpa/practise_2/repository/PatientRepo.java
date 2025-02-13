package com.sathwick.learning.jpa.practise_2.repository;

import com.sathwick.learning.jpa.practise_2.domain.Patient2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepo extends JpaRepository<Patient2, Integer> {
}
