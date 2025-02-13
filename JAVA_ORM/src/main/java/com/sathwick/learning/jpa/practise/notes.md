## Patient Scheduling system

**ENTITIES**

- Patient
- Doctor
- Appointment


**Relationships**

- Patient 1 -----------> * Appointments
- Doctor 1 -----------> * Appointments
- Patient * ----------> * Doctor


-- Break down
> - Patient table (id, name, phone)
> - Doctor table (id, name, qualification)
> - Appointment (id, patient_id, doctor_id, datetime)
> - patients_doctors table (patient_id, doctor_id)


-- Database schema

```
use user;

create table patient2(
    id int primary key,
    first_name varchar(20),
    last_name varchar(20),
    phone varchar(20),
    provider_name varchar(30),
    copay decimal(10.5)
);

create table doctor(
    id int primary key,
    first_name varchar(20),
    last_name varchar(30),
    speciality varchar(30)
);

create table patients_doctors(
    patient_id int,
    doctor_id int,
    foreign key(patient_id) references patient2(id),
    foreign key(doctor_id) references doctor(id)
);

create table appointment(
    id int primary key,
    patient_id int,
    doctor_id int,
    appointment_time datetime,
    started tinyint(1),
    ended tinyint(1),
    reason varchar(200),
    foreign key(patient_id) references patient2(id),
    foreign key(doctor_id) references doctor(id)
);
```