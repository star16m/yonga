package com.yonga.auc.data.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, String> {
    List<Customer> findCustonersByDisplayTrue();
    Customer findByUserId(String userId);
}
