package com.jiyuu.banking.service;

import com.jiyuu.banking.audit.annotation.Auditable;
import com.jiyuu.banking.dto.TransactionRequest;
import com.jiyuu.banking.dto.TransactionResponse;
import com.jiyuu.banking.entity.*;
import com.jiyuu.banking.enums.Currency;
import com.jiyuu.banking.enums.TransactionStatus;
import com.jiyuu.banking.enums.TransactionType;
import com.jiyuu.banking.exception.ResourceNotFoundException;
import com.jiyuu.banking.exception.ValidationException;
import com.jiyuu.banking.repository.AccountMembershipRepository;
import com.jiyuu.banking.repository.AccountRepository;
import com.jiyuu.banking.repository.LoanInstallmentRepository;
import com.jiyuu.banking.repository.TransactionsRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class TransactionsService {
    private final TransactionsRepository transactionsRepository;
    private final AccountRepository accountRepository;
    private final AccountMembershipRepository accountMembershipRepository;
    private final ProcessTransactionService processTransactionService;

    public Transactions createTransactionEntity(TransactionRequest request, Card card) {
        Account source = null;
        Account target = null;

        if (request.target() != null) {
            target = accountRepository.findBynumeroAccount(request.target())
                    .orElseThrow(() -> new ResourceNotFoundException("Le compte de destination n'existe pas"));
        }

        if (request.source() != null) {
            source = accountRepository.findBynumeroAccount(request.source())
                    .orElseThrow(() -> new ResourceNotFoundException("Le compte de source n'existe pas"));
        }

        Transactions tx = Transactions.builder()
                .transactionRef(UUID.randomUUID().toString())
                .currencyTransaction(Currency.valueOf(request.currency()))
                .transactionType(TransactionType.valueOf(request.type()))
                .amountTransaction(request.amount())
                .transactionStatus(TransactionStatus.PENDING)
                .sourceAccount(source)
                .targetAccount(target)
                .card(card)
                .build();

        tx = transactionsRepository.save(tx);
        long txId = tx.getIdTransaction();

        try {
            this.processTransactionService.process(request);
            transactionsRepository.updateStatus(txId, TransactionStatus.COMPLETED);
        } catch (Exception e) {
            transactionsRepository.updateStatus(txId, TransactionStatus.FAILED);
            throw e;
        }

        return transactionsRepository.findById(txId).orElseThrow();
    }

    public TransactionResponse createTransaction(TransactionRequest request, Card card) {
        return toResponse(createTransactionEntity(request, card));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(Long idTransaction, TransactionStatus transactionStatus) {
        Transactions tx = transactionsRepository.findById(idTransaction)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        tx.setTransactionStatus(transactionStatus);
    }

    public TransactionResponse toResponse(Transactions tx) {
        String source = resolverAccountNumber(tx.getSourceAccount());
        String target = resolverAccountNumber(tx.getTargetAccount());

        return TransactionResponse.of(tx, source, target);
    }

    private String resolverAccountNumber(Account account) {
        if (account == null) return null;

        return this.accountMembershipRepository.findByAccount_idAccount(account.getIdAccount())
                .stream()
                .filter(AccountMembership::isPrimary)
                .map(m -> m.getAccount().getNumeroAccount())
                .findFirst()
                .orElse(null);
    }

    @Auditable(action = "READ", entity = "TRANSACTION")
    public TransactionResponse getTransactionByRef(String ref) {
        Transactions tx = this.transactionsRepository.findByTransactionRef(ref)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        return toResponse(tx);
    }

    @Transactional
    public TransactionResponse reverseTransaction(String ref) {
        Transactions originalTx = this.transactionsRepository.findByTransactionRef(ref)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (LocalDateTime.now().isAfter(originalTx.getCreatedAt().plusHours(24))) {
            throw new ValidationException("Impossible d'annuler la transaction après 24 heures");
        }

        if (originalTx.getTransactionStatus() == TransactionStatus.REVERSED) {
            throw new ValidationException("Transaction déjà annulée");
        }

        if (originalTx.getTransactionStatus() != TransactionStatus.COMPLETED) {
            throw new ValidationException("Transaction non annulable");
        }

        Account source = originalTx.getSourceAccount();
        Account target = originalTx.getTargetAccount();
        BigDecimal amount = originalTx.getAmountTransaction();

        if (source != null) {
            source.setSoldeAccount(source.getSoldeAccount().add(amount));
        }

        if (target != null) {
            target.setSoldeAccount(target.getSoldeAccount().subtract(amount));
        }

        Transactions reversedTx = Transactions.builder()
                .transactionRef(UUID.randomUUID().toString())
                .originalTransactionRef(originalTx.getTransactionRef())
                .currencyTransaction(originalTx.getCurrencyTransaction())
                .transactionType(TransactionType.REVERSAL)
                .amountTransaction(originalTx.getAmountTransaction())
                .transactionStatus(TransactionStatus.COMPLETED)
                .sourceAccount(source)
                .targetAccount(target)
                .build();

        reversedTx = transactionsRepository.save(reversedTx);

        originalTx.setTransactionStatus(TransactionStatus.REVERSED);

        return toResponse(reversedTx);
    }

}
