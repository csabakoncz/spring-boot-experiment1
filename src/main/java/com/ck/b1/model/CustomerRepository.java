package com.ck.b1.model;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface CustomerRepository extends CrudRepository<Customer, Long> {

    List<Customer> findByLastName(String lastName);

    Customer findById(long id);

    @Query("select c from Customer c where c.lastName=?1 or c.firstName=?1")
    List<Customer> findByName(String name);

    @Query(value = "select * from customer where lower(last_name)=lower(?1) or lower(first_name)=lower(?1)", nativeQuery = true)
    List<Customer> findByNameIC(String name);

}