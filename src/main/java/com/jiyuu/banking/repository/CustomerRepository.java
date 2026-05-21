package com.jiyuu.banking.repository;

import com.jiyuu.banking.entity.Customer;
import com.jiyuu.banking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {
    Optional<Customer> findByUser(User user);

    Optional<Customer> findBytelephoneCustomer(String telephoneCustomer);
}
