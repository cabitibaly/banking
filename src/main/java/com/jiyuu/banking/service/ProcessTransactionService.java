package com.jiyuu.banking.service;

import com.jiyuu.banking.audit.annotation.Auditable;
import com.jiyuu.banking.dto.TransactionRequest;
import com.jiyuu.banking.entity.Account;
import com.jiyuu.banking.entity.Loan;
import com.jiyuu.banking.entity.LoanInstallment;
import com.jiyuu.banking.enums.AccountStatus;
import com.jiyuu.banking.enums.InstallmentStatus;
import com.jiyuu.banking.enums.LoanStatus;
import com.jiyuu.banking.exception.InsufficientFundsException;
import com.jiyuu.banking.exception.ResourceNotFoundException;
import com.jiyuu.banking.exception.ValidationException;
import com.jiyuu.banking.repository.AccountRepository;
import com.jiyuu.banking.repository.LoanInstallmentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class ProcessTransactionService {
    private final AccountRepository accountRepository;

//    @Auditable(action = "CREATE", entity = "TRANSACTION")
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    public void process(TransactionRequest request) {
        switch (request.type()) {
            case "DEPOSIT" -> deposit(request);
            case "WITHDRAW", "REPAYMENT" -> withdrawAndRepayment(request);
            case "TRANSFER" -> transfer(request);
            case "FEE" -> fee(request);
            case "INTEREST" -> interest(request);
            default -> throw new ValidationException("Le type de transaction est invalide");
        }
    }

    private void deposit(TransactionRequest request) {
        Account target = this.accountRepository.findBynumeroAccount(request.target())
                .orElseThrow(() -> new ResourceNotFoundException("Le compte de destination n'existe pas"));

        if (target.getAccountStatus() != AccountStatus.ACTIVE) {
            throw  new ValidationException("Le compte n'est pas actif");
        }

        target.setSoldeAccount(target.getSoldeAccount().add(request.amount()));
    }

    private void withdrawAndRepayment(TransactionRequest request) {
        Account source = this.accountRepository.findBynumeroAccount(request.source())
                .orElseThrow(() -> new ResourceNotFoundException("Le compte de source n'existe pas"));

        if (source.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new ValidationException("Le compte n'est pas actif");
        }

        BigDecimal finalSolde = source.getSoldeAccount().subtract(request.amount());
        BigDecimal limitDecouvert = source.getDecouvert().negate();

        if (finalSolde.compareTo(limitDecouvert) < 0) {
            throw new ValidationException("Impossible de retirer le montant demandé car le solde dépassera la limite de découvert autorisée");
        }

        source.setSoldeAccount(source.getSoldeAccount().subtract(request.amount()));
    }

    private void transfer(TransactionRequest request) {
        Account source = this.accountRepository.findBynumeroAccount(request.source())
                .orElseThrow(() -> new ResourceNotFoundException("Le compte de source n'existe pas"));

        Account target = this.accountRepository.findBynumeroAccount(request.target())
                .orElseThrow(() -> new ResourceNotFoundException("Le compte de destination n'existe pas"));

        if ((source.getAccountStatus() != AccountStatus.ACTIVE) || (target.getAccountStatus() != AccountStatus.ACTIVE)) {
            throw new ValidationException("Verifiez que les comptes sont actifs");
        }

        BigDecimal finalSolde = source.getSoldeAccount().subtract(request.amount());
        BigDecimal limitDecouvert = source.getDecouvert().negate();
        if (finalSolde.compareTo(limitDecouvert) < 0) {
            throw new ValidationException("Impossible de transférer le montant demandé car le solde dépassera la limite de découvert autorisée");
        }

        source.setSoldeAccount(source.getSoldeAccount().subtract(request.amount()));
        target.setSoldeAccount(target.getSoldeAccount().add(request.amount()));
    }

    private void fee(TransactionRequest request) {
        Account source = this.accountRepository.findBynumeroAccount(request.source())
                .orElseThrow(() -> new ResourceNotFoundException("Le compte de source n'existe pas"));

        if (source.getAccountStatus() == AccountStatus.CLOSED) {
            throw new ValidationException("Le compte est clôturé");
        }

        if (source.getAccountStatus() == AccountStatus.SUSPENDED && request.type().equals("FEE")) {
            throw new ValidationException("Le compte est suspendu: frais interdits");
        }

        BigDecimal finalSolde = source.getSoldeAccount().subtract(request.amount());
        BigDecimal limitDecouvert = source.getDecouvert().negate();
        if (finalSolde.compareTo(limitDecouvert) < 0) {
            throw new ValidationException("Impossible d'effectuer l'opération car le solde dépassera la limite de découvert autorisée");
        }

        source.setSoldeAccount(source.getSoldeAccount().subtract(request.amount()));
    }

    private void interest(TransactionRequest request) {
        Account source = this.accountRepository.findBynumeroAccount(request.source())
                .orElseThrow(() -> new ResourceNotFoundException("Le compte de source n'existe pas"));

        if (source.getAccountStatus() == AccountStatus.CLOSED) {
            throw new ValidationException("Le compte est clôturé");
        }

        BigDecimal finalSolde = source.getSoldeAccount().subtract(request.amount());
        BigDecimal limitDecouvert = source.getDecouvert().negate();
        if (finalSolde.compareTo(limitDecouvert) < 0) {
            throw new InsufficientFundsException("Solde insuffisant pour effectuer l'opération");
        }

        source.setSoldeAccount(finalSolde);
    }
}
