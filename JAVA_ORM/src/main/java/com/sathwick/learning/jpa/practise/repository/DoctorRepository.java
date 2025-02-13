package com.sathwick.learning.jpa.practise.repository;

import com.sathwick.learning.jpa.practise.domain.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {
}
