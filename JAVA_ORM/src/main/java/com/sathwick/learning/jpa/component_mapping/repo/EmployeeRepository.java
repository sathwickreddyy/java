package com.sathwick.learning.jpa.component_mapping.repo;

import com.sathwick.learning.jpa.component_mapping.domain.Employee;
import org.springframework.data.repository.CrudRepository;

public interface EmployeeRepository extends CrudRepository<Employee, Integer> {
}
