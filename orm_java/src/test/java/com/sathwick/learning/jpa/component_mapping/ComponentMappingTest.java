package com.sathwick.learning.jpa.component_mapping;

import com.sathwick.learning.jpa.component_mapping.domain.Address;
import com.sathwick.learning.jpa.component_mapping.domain.Employee;
import com.sathwick.learning.jpa.component_mapping.repo.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ComponentMappingTest {

    @Autowired
    EmployeeRepository employeeRepository;

    @Test
    public void testCreate(){
        Address address = new Address();
        address.setCity("Bangalore");
        address.setCountry("India");
        address.setState("Karnataka");
        address.setStreetaddress("Brigham City");
        address.setZipcode("56013");

        Employee employee = new Employee();
        employee.setName("John");
        employee.setId(1234);
        employee.setAddress(address);

        employeeRepository.save(employee);
    }

}