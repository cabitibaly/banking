package com.jiyuu.banking.dto;

import com.jiyuu.banking.entity.Customer;

import java.time.LocalDateTime;

public record CustomerResponse(
        long id,
        String numero,
        String nom,
        String email,
        String telephone,
        LocalDateTime dateNaissance,
        String statusCustomer,
        LocalDateTime dateCreation,
        LocalDateTime dateModification
) {
    public static CustomerResponse of(Customer customer) {
        return new CustomerResponse(
                customer.getIdCustomer(),
                customer.getNumeroCustomer(),
                customer.getNomCustomer(),
                customer.getUser().getEmail(),
                customer.getTelephoneCustomer(),
                customer.getDateNaissance(),
                customer.getStatusCustomer().toString(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
