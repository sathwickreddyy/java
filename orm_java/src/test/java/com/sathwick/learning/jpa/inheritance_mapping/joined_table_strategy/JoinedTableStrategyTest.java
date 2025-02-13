package com.sathwick.learning.jpa.inheritance_mapping.joined_table_strategy;

import com.sathwick.learning.jpa.inheritance_mapping.joined_table_strategy.domain.Check;
import com.sathwick.learning.jpa.inheritance_mapping.joined_table_strategy.domain.CreditCard;
import com.sathwick.learning.jpa.inheritance_mapping.joined_table_strategy.repository.PaymentRepository2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JoinedTableStrategyTest {

    @Autowired
    PaymentRepository2 paymentRepository;
    @Test
    public void createCreditCardPayment() {
        CreditCard cc = new CreditCard();
        cc.setId(123);
        cc.setAmount(100);
        cc.setCardnumber("123456789");
        paymentRepository.save(cc);
    }

    @Test
    public void createCheckPayment() {
        Check ch = new Check();
        ch.setId(124);
        ch.setAmount(200);
        ch.setChecknumber("123456789");
        paymentRepository.save(ch);
    }

    /*
        Hibernate: select cc1_0.id,cc1_1.amount,cc1_0.cardnumber from card cc1_0 join payment cc1_1 on cc1_0.id=cc1_1.id where cc1_0.id=?
        Hibernate: insert into payment (amount,id) values (?,?)
        Hibernate: insert into card (cardnumber,id) values (?,?)
        Hibernate: select c1_0.id,c1_1.amount,c1_0.checknumber from bankcheck c1_0 join payment c1_1 on c1_0.id=c1_1.id where c1_0.id=?
        Hibernate: insert into payment (amount,id) values (?,?)
        Hibernate: insert into bankcheck (checknumber,id) values (?,?)
     */
}