package com.jiyuu.banking.repository;

import com.jiyuu.banking.entity.Customer;
import com.jiyuu.banking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUser(User user);
}
