package com.sathwick.learning.jpa.practise_2.repository;

import com.sathwick.learning.jpa.practise_2.domain.ClinicalData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClinicalDataRepository extends JpaRepository<ClinicalData, Integer> {
}
