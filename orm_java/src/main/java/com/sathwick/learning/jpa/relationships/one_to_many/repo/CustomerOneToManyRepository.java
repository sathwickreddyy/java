package com.sathwick.learning.jpa.relationships.one_to_many.repo;

import com.sathwick.learning.jpa.relationships.one_to_many.domain.Customer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerOneToManyRepository extends CrudRepository<Customer, Integer> {
}
