package com.sathwick.learning.jpa.practise.repository;

import com.sathwick.learning.jpa.practise.domain.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
}
