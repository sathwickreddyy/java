//package com.sathwick.learning.jpa.inheritance_mapping.single_table_strategy;
//
//import com.sathwick.learning.jpa.inheritance_mapping.single_table_strategy.domain.Check;
//import com.sathwick.learning.jpa.inheritance_mapping.single_table_strategy.domain.CreditCard;
//import com.sathwick.learning.jpa.inheritance_mapping.single_table_strategy.repository.PaymentRepository;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//@SpringBootTest
//class SingleTableStrategyTest {
//
//    @Autowired
//    PaymentRepository paymentRepository;
//
//    @Test
//    public void contextLoads(){
//
//    }
//
////    @Test
////    public void createCreditCardPayment() {
////        CreditCard cc = new CreditCard();
////        cc.setId(123);
////        cc.setAmount(100);
////        cc.setCardnumber("123456789");
////        paymentRepository.save(cc);
////    }
////
////    @Test
////    public void createCheckPayment() {
////        Check ch = new Check();
////        ch.setId(124);
////        ch.setAmount(200);
////        ch.setChecknumber("123456789");
////        paymentRepository.save(ch);
////    }
//
//}