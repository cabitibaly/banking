package com.jiyuu.banking.dto;

import com.jiyuu.banking.entity.Account;
import com.jiyuu.banking.entity.Customer;

import java.time.LocalDateTime;

public record MemebershipResponse(
        long idAccount,
        long idCustomer,
        String accountNumber,
        String customerName,
        boolean isPrimary,
        LocalDateTime dateAdhesion
) {
    public static MemebershipResponse of(Account account, Customer customer, boolean isPrimary, LocalDateTime dateAdhesion) {
        return new MemebershipResponse(
                account.getIdAccount(),
                customer.getIdCustomer(),
                account.getNumeroAccount(),
                customer.getNomCustomer(),
                isPrimary,
                dateAdhesion
        );
    }
}
