package com.ck.b1.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest()
public class CustomerRepositoryTest {

    @Autowired
    CustomerRepository customerRepository;

    @Test
    void canCreateAndFindCustomers() {
        var c1 = new Customer("First1", "Csaba");
        var c2 = new Customer("Csaba", "Last2");
        var c3 = new Customer("First3", "Last3");

        customerRepository.save(c1);
        customerRepository.save(c2);
        customerRepository.save(c3);

        Assertions.assertEquals(3, customerRepository.count());

        // H2 seems case sensitive
        var csabaList = customerRepository.findByName("Csaba");
        Assertions.assertEquals(2, csabaList.size());

        var csabaListIC = customerRepository.findByNameIC("cSaba");
        Assertions.assertEquals(2, csabaListIC.size());
    }

}
