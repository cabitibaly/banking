package com.jiyuu.banking.service;

import com.jiyuu.banking.audit.annotation.Auditable;
import com.jiyuu.banking.dto.TransactionRequest;
import com.jiyuu.banking.entity.Account;
import com.jiyuu.banking.exception.InsufficientFundsException;
import com.jiyuu.banking.exception.ResourceNotFoundException;
import com.jiyuu.banking.exception.ValidationException;
import com.jiyuu.banking.repository.AccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ProcessTransactionService {
    private final AccountRepository accountRepository;

//    @Auditable(action = "CREATE", entity = "TRANSACTION")
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    public void process(TransactionRequest request) {
        switch (request.type()) {
            case "DEPOSIT" -> deposit(request);
            case "WITHDRAW" -> withdraw(request);
            case "TRANSFER" -> transfer(request);
            case "INTEREST" -> interest(request);
            case "FEE" -> fee(request);
            default -> throw new ValidationException("Le type de transaction est invalide");
        }
    }

    private void deposit(TransactionRequest request) {
        Account target = this.accountRepository.findById(request.target())
                .orElseThrow(() -> new ResourceNotFoundException("Le compte de destination n'existe pas"));

        target.setSoldeAccount(target.getSoldeAccount().add(request.amount()));
    }

    private void withdraw(TransactionRequest request) {
        Account source = this.accountRepository.findById(request.source())
                .orElseThrow(() -> new ResourceNotFoundException("Le compte de source n'existe pas"));

        if (source.getSoldeAccount().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException("Le solde de l'account est insuffisant");
        }

        source.setSoldeAccount(source.getSoldeAccount().subtract(request.amount()));
    }

    private void transfer(TransactionRequest request) {
        Account source = this.accountRepository.findById(request.source())
                .orElseThrow(() -> new ResourceNotFoundException("Le compte de source n'existe pas"));

        Account target = this.accountRepository.findById(request.target())
                .orElseThrow(() -> new ResourceNotFoundException("Le compte de destination n'existe pas"));

        source.setSoldeAccount(source.getSoldeAccount().subtract(request.amount()));
        target.setSoldeAccount(target.getSoldeAccount().add(request.amount()));
    }

    private void interest(TransactionRequest request) {
        Account source = this.accountRepository.findById(request.source())
                .orElseThrow(() -> new ResourceNotFoundException("Le compte de source n'existe pas"));

        source.setSoldeAccount(source.getSoldeAccount().subtract(request.amount()));
    }

    private void fee(TransactionRequest request) {
        Account source = this.accountRepository.findById(request.source())
                .orElseThrow(() -> new ResourceNotFoundException("Le compte de source n'existe pas"));

        source.setSoldeAccount(source.getSoldeAccount().subtract(request.amount()));
    }
}
