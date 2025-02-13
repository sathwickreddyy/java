//package com.sathwick.learning.jpa.relationships.one_to_many;
//
//import com.sathwick.learning.jpa.relationships.one_to_many.domain.Customer;
//import com.sathwick.learning.jpa.relationships.one_to_many.domain.PhoneNumber;
//import com.sathwick.learning.jpa.relationships.one_to_many.repo.CustomerOneToManyRepository;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.HashSet;
//import java.util.Optional;
//import java.util.Set;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//class OneToManyTest {
//
//    @Autowired
//    CustomerOneToManyRepository repository;
//
//    @Test
//    public void testCreateCustomer(){
//        Customer customer = new Customer();
//        customer.setName("Sathwick");
//
//        PhoneNumber p1 = new PhoneNumber();
//        p1.setNumber("9882891231");
//        p1.setType("cell");
//        PhoneNumber p2 = new PhoneNumber();
//        p2.setNumber("9882891232");
//        p2.setType("office");
//        customer.addPhoneNumber(p1);
//        customer.addPhoneNumber(p2);
//
//        repository.save(customer);
//    }
//
//    @Test
//    @Transactional // to fix lazy loading issue
//    public void testReadCustomer(){
//        Optional<Customer> optionalCustomer = repository.findById(452);
////        assertTrue(optionalCustomer.isPresent());
////        System.out.println("Bunny "+optionalCustomer.get().getNumbers());
//        /*
//            Error:
//            org.hibernate.LazyInitializationException: failed to lazily initialize a collection of role:
//                com.sathwick.learning.jpa.relationships.one_to_many.domain.Customer2.numbers:
//                could not initialize proxy - no Session
//         */
//        /*
//            Lazy Loading?
//                When objects are associated, when the parent object is loaded, the associated objects are not loaded
//                with it. This is called Lazy Loading. In Hibernate, this is the default behavior, but you can change
//                it by using the fetch type attribute.
//
//                In our use case
//                Customer2 Class
//                    List<PhoneNumber> phoneNumbers;
//                        if we are fetching lazily then
//                            > When we load customer obbject, phoneNumbers will be empty
//                            > When we call getNumbers() on customer object, it will load phoneNumbers on demand
//
//
//             To fix above error -> we can change fetch type to EAGER in Customer2 Class
//         */
//        Customer customer = optionalCustomer.get();
//        System.out.println(customer);
//        Set<PhoneNumber> numbers = customer.getNumbers(); // transactional invokes lazy loading on demand
//        numbers.forEach(number -> System.out.println(number.getNumber()));
//    }
//
//    @Test
//    public void testUpdateCustomer(){
//        Optional<Customer> optionalCustomer = repository.findById(452);
//        assertTrue(optionalCustomer.isPresent());
//        Customer customer = optionalCustomer.get();
//
//        customer.setName("Sathwick Reddy");
//        customer.getNumbers().forEach(number -> number.setType("cell"));
//        repository.save(customer); // save will update the customer and associated phone numbers as cascading is set to ALL
//    }
//
//    @Test
//    public void testDeleteCustomer(){
//        repository.deleteById(452);
//        Optional<Customer> optionalCustomer = repository.findById(452);
//        assertFalse(optionalCustomer.isPresent());
//    }
//
//}
