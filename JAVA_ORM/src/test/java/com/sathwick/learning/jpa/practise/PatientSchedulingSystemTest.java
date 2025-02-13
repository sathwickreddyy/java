package com.sathwick.learning.jpa.practise;

import com.sathwick.learning.jpa.practise.domain.Appointment;
import com.sathwick.learning.jpa.practise.domain.Doctor;
import com.sathwick.learning.jpa.practise.domain.Insurance;
import com.sathwick.learning.jpa.practise.domain.Patient;
import com.sathwick.learning.jpa.practise.repository.AppointmentRepository;
import com.sathwick.learning.jpa.practise.repository.DoctorRepository;
import com.sathwick.learning.jpa.practise.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PatientSchedulingSystemTest {
    @Autowired
    DoctorRepository doctorRepository;
    @Autowired
    PatientRepository patientRepository;
    @Autowired
    AppointmentRepository appointmentRepository;

    @Test
    void testCreate() {
        assertNotNull(doctorRepository);
        assertNotNull(patientRepository);
        assertNotNull(appointmentRepository);
    }

    @Test
    void testCreateDoctor() {
        Doctor doctor = new Doctor();
        doctor.setFirstName("Sathwick");
        doctor.setLastName("Reddy");
        doctor.setSpeciality("Cardiology");
        doctorRepository.save(doctor);
        assertNotNull(doctor.getId());
    }

    @Test
    void testCreatePatient() {
        Patient patient = new Patient();
        patient.setFirstName("Saketh");
        patient.setLastName("Reddy");
        patient.setPhone("9666999922");
        Insurance insurance = new Insurance();
        insurance.setProviderName("Blue Cross Blue Shield");
        insurance.setCopay(100d);
        patient.setInsurance(insurance);

        Doctor doctor = doctorRepository.findById(1).get();
        List<Doctor> doctors = Arrays.asList(doctor);

        patient.setDoctors(doctors);
        patientRepository.save(patient);
    }

    @Test
    void testCreateAppointment() {
        Appointment appointment = new Appointment();

        Timestamp appointmentTime = new Timestamp(new Date().getTime());

        appointment.setAppointmentTime(appointmentTime);
        appointment.setPatient(patientRepository.findById(1).get());
        appointment.setDoctor(doctorRepository.findById(1).get());
        appointment.setStarted(true);
        appointment.setReason("I have fever");

        appointmentRepository.save(appointment);
    }
}