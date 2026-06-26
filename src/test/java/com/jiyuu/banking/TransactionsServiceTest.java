package com.jiyuu.banking;

import com.jiyuu.banking.dto.TransactionRequest;
import com.jiyuu.banking.dto.TransactionResponse;
import com.jiyuu.banking.entity.Account;
import com.jiyuu.banking.entity.Transactions;
import com.jiyuu.banking.enums.*;
import com.jiyuu.banking.exception.ResourceNotFoundException;
import com.jiyuu.banking.repository.AccountRepository;
import com.jiyuu.banking.repository.TransactionsRepository;
import com.jiyuu.banking.service.ProcessTransactionService;
import com.jiyuu.banking.service.TransactionsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionsServiceTest {
    @Mock
    private TransactionsRepository transactionsRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ProcessTransactionService processTransactionService;

    @InjectMocks
    private TransactionsService transactionsService;

    @Test
    public void shouldThrowResourceNotFoundWhenTargetIsNotFound() {
        when(this.accountRepository.findBynumeroAccount("AC-TARGET-001"))
                .thenReturn(Optional.empty());

        TransactionRequest request = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(100000))
                .currency("XOF")
                .type("DEPOSIT")
                .source(null)
                .target("AC-TARGET-001")
                .build();

        assertThrows(
                ResourceNotFoundException.class,
                () -> this.transactionsService.createTransactionEntity(request, null)
        );
    }

    @Test
    public void shouldThrowResourceNotFoundWhenSourceIsNotFound() {
        when(this.accountRepository.findBynumeroAccount("AC-SOURCE-001"))
                .thenReturn(Optional.empty());

        TransactionRequest request = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(100000))
                .currency("XOF")
                .type("DEPOSIT")
                .source("AC-SOURCE-001")
                .target(null)
                .build();

        assertThrows(
                ResourceNotFoundException.class,
                () -> this.transactionsService.createTransactionEntity(request, null)
        );
    }

    @Test
    public void shouldReturnTransactionEntityWhenSourceIsDefineAndNotTarget() {
        Account source = this.buildAccount("AC-SOURCE-001");

        TransactionRequest request = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(100000))
                .currency("XOF")
                .type("DEPOSIT")
                .source("AC-SOURCE-001")
                .target(null)
                .build();

        Transactions transactions = this.buildTransaction(request, source, null);

        when(this.accountRepository.findBynumeroAccount("AC-SOURCE-001"))
                .thenReturn(Optional.of(source));

        when(this.transactionsRepository.save(any(Transactions.class)))
                .thenReturn(transactions);

        doNothing().when(this.processTransactionService)
                .process(request);

        transactions.setTransactionStatus(TransactionStatus.COMPLETED);

        when(this.transactionsRepository.findById(1L))
                .thenReturn(Optional.of(transactions));

        Transactions transactionSave = this.transactionsService.createTransactionEntity(request, null);

        verify(this.transactionsRepository).save(any(Transactions.class));
        verify(this.processTransactionService).process(request);
        assertEquals(TransactionStatus.COMPLETED, transactionSave.getTransactionStatus());
    }

    @Test
    public void shouldReturnTransactionEntityWhenTargetIsDefineAndNotSource() {
        Account target = this.buildAccount("AC-TARGET-001");

        TransactionRequest request = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(100000))
                .currency("XOF")
                .type("DEPOSIT")
                .source(null)
                .target("AC-TARGET-001")
                .build();

        Transactions transactions = this.buildTransaction(request, null, target);

        when(this.accountRepository.findBynumeroAccount("AC-TARGET-001"))
                .thenReturn(Optional.of(target));

        when(this.transactionsRepository.save(any(Transactions.class)))
                .thenReturn(transactions);

        doNothing().when(this.processTransactionService)
                .process(request);

        transactions.setTransactionStatus(TransactionStatus.COMPLETED);

        when(this.transactionsRepository.findById(1L))
                .thenReturn(Optional.of(transactions));

        Transactions transactionSave = this.transactionsService.createTransactionEntity(request, null);

        verify(this.transactionsRepository).save(any(Transactions.class));
        verify(this.processTransactionService).process(request);
        assertEquals(TransactionStatus.COMPLETED, transactionSave.getTransactionStatus());
    }

    @Test
    public void shouldReturnTransactionEntityWhenTargetAndSourceAreDefine() {
        Account target = this.buildAccount("AC-TARGET-001");
        Account source = this.buildAccount("AC-SOURCE-001");

        TransactionRequest request = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(100000))
                .currency("XOF")
                .type("DEPOSIT")
                .source("AC-SOURCE-001")
                .target("AC-TARGET-001")
                .build();

        Transactions transactions = this.buildTransaction(request, source, target);

        when(this.accountRepository.findBynumeroAccount("AC-TARGET-001"))
                .thenReturn(Optional.of(target));

        when(this.accountRepository.findBynumeroAccount("AC-SOURCE-001"))
                .thenReturn(Optional.of(source));

        when(this.transactionsRepository.save(any(Transactions.class)))
                .thenReturn(transactions);

        doNothing().when(this.processTransactionService)
                .process(request);

        transactions.setTransactionStatus(TransactionStatus.COMPLETED);

        when(this.transactionsRepository.findById(1L))
                .thenReturn(Optional.of(transactions));

        Transactions transactionSave = this.transactionsService.createTransactionEntity(request, null);

        verify(this.transactionsRepository).save(any(Transactions.class));
        verify(this.processTransactionService).process(request);
        assertEquals(TransactionStatus.COMPLETED, transactionSave.getTransactionStatus());
    }

    @Test
    public void shouldThrowExceptionWhenProcessIsCalled() {
        Account target = this.buildAccount("AC-TARGET-001");
        Account source = this.buildAccount("AC-SOURCE-001");

        TransactionRequest request = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(100000))
                .currency("XOF")
                .type("DEPOSIT")
                .source("AC-SOURCE-001")
                .target("AC-TARGET-001")
                .build();

        Transactions transactions = this.buildTransaction(request, source, target);

        when(this.accountRepository.findBynumeroAccount("AC-TARGET-001"))
                .thenReturn(Optional.of(target));

        when(this.accountRepository.findBynumeroAccount("AC-SOURCE-001"))
                .thenReturn(Optional.of(source));

        when(this.transactionsRepository.save(any(Transactions.class)))
                .thenReturn(transactions);

        doThrow(new RuntimeException()).when(this.processTransactionService)
                .process(request);

        doNothing().when(this.transactionsRepository)
            .updateStatus(1L, TransactionStatus.FAILED);

        assertThrows(
                RuntimeException.class,
                () -> this.transactionsService.createTransactionEntity(request, null)
        );

        verify(this.transactionsRepository).updateStatus(1L, TransactionStatus.FAILED);
    }

    private Account buildAccount(String accountNumber) {
        return Account.builder()
                .idAccount(1L)
                .soldeAccount(BigDecimal.valueOf(10000000))
                .decouvert(BigDecimal.valueOf(50000))
                .currencyAccount(Currency.XOF)
                .numeroAccount(accountNumber)
                .accountStatus(AccountStatus.ACTIVE)
                .accountType(AccountType.CHECKING)
                .build();
    }

    private Transactions buildTransaction(TransactionRequest request, Account source, Account target) {
        return Transactions.builder()
                .idTransaction(1L)
                .transactionRef(UUID.randomUUID().toString())
                .currencyTransaction(Currency.valueOf(request.currency()))
                .transactionType(TransactionType.valueOf(request.type()))
                .amountTransaction(request.amount())
                .transactionStatus(TransactionStatus.PENDING)
                .sourceAccount(source)
                .targetAccount(target)
                .card(null)
                .build();
    }
}
