package com.ck.b1;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.ck.b1.model.Customer;
import com.ck.b1.model.CustomerRepository;

@ActiveProfiles("test")
@SpringBootTest()
public class JPATests {

    @Autowired
    CustomerRepository repository;

    @Test
    void jpaWorks() {
        var count1 = repository.count();

        var customer = new Customer("Elek", "Rongy");
        repository.save(customer);

        Assertions.assertEquals(count1 + 1, repository.count());
    }
}
