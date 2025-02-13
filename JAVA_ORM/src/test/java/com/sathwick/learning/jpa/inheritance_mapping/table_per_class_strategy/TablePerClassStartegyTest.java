//package com.sathwick.learning.jpa.inheritance_mapping.table_per_class_strategy;
//
//import com.sathwick.learning.jpa.inheritance_mapping.table_per_class_strategy.domain.Check;
//import com.sathwick.learning.jpa.inheritance_mapping.table_per_class_strategy.domain.CreditCard;
//import com.sathwick.learning.jpa.inheritance_mapping.table_per_class_strategy.repository.PaymentRepository2;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//@SpringBootTest
//class TablePerClassStartegyTest {
//    @Autowired
//    PaymentRepository2 paymentRepository;
//    @Test
//    public void createCreditCardPayment() {
//        CreditCard cc = new CreditCard();
//        cc.setId(123);
//        cc.setAmount(100);
//        cc.setCardnumber("123456789");
//        paymentRepository.save(cc);
//    }
//
//    @Test
//    public void createCheckPayment() {
//        Check ch = new Check();
//        ch.setId(124);
//        ch.setAmount(200);
//        ch.setChecknumber("123456789");
//        paymentRepository.save(ch);
//    }
//}