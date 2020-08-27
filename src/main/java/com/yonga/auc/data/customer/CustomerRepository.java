package com.yonga.auc.data.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, String> {
    List<Customer> findCustonersByDisplayTrue();
    Customer findByUserId(String userId);
    @Query("select c from Customer c where c.userId <> 'star16m'")
    List<Customer> findAllCustomer();
}
