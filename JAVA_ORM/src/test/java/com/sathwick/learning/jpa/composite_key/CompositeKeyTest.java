package com.sathwick.learning.jpa.composite_key;

import com.sathwick.learning.jpa.composite_key.domain.Customer2;
import com.sathwick.learning.jpa.composite_key.domain.CustomerId;
import com.sathwick.learning.jpa.composite_key.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CompositeKeyTest {

    @Autowired
    CustomerRepository repository;


    @Test
    void testSaveCustomer(){
        Customer2 customer2 = new Customer2();
        CustomerId customerId = new CustomerId();
        customerId.setId(23456);
        customerId.setEmail("XXXXXXXXXXXXX");
//
//        customer2.setId(1234);
//        customer2.setEmail("test@test.com");
        customer2.setCustomerId(customerId);
        customer2.setName("bunny");
        repository.save(customer2);
    }

}