package com.sathwick.learning.jpa.inheritance_mapping.joined_table_strategy.repository;

import com.sathwick.learning.jpa.inheritance_mapping.joined_table_strategy.domain.Payment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository2 extends CrudRepository<Payment, Integer> {

}
