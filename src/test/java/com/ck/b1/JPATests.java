package com.ck.b1;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ck.b1.model.Customer;
import com.ck.b1.model.CustomerRepository;

@SpringBootTest
public class JPATests {

	@Autowired
	CustomerRepository repository;
	
	@Test
	void jpaWorks() {
		var customer = new Customer("Elek", "Rongy");
		repository.save(customer);
	
		Assertions.assertEquals(1,repository.count());
	}
}
