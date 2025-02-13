package com.sathwick.learning.jpa.composite_key.repository;

import com.sathwick.learning.jpa.composite_key.domain.Customer2;
import com.sathwick.learning.jpa.composite_key.domain.CustomerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer2, CustomerId> {
}
