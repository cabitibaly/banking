package com.jiyuu.banking.service;

import com.jiyuu.banking.dto.DocumentRequest;
import com.jiyuu.banking.dto.LoanBaseResponse;
import com.jiyuu.banking.dto.LoanRequest;
import com.jiyuu.banking.entity.Account;
import com.jiyuu.banking.entity.Customer;
import com.jiyuu.banking.entity.Loan;
import com.jiyuu.banking.entity.LoanDocument;
import com.jiyuu.banking.enums.DocumentType;
import com.jiyuu.banking.enums.LoanStatus;
import com.jiyuu.banking.enums.LoanType;
import com.jiyuu.banking.exception.ResourceNotFoundException;
import com.jiyuu.banking.exception.ValidationException;
import com.jiyuu.banking.repository.AccountRepository;
import com.jiyuu.banking.repository.CustomerRepository;
import com.jiyuu.banking.repository.LoanDocumentRepository;
import com.jiyuu.banking.repository.LoanRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class LoanService {
    private final LoanRepository loanRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final LoanDocumentRepository loanDocumentRepository;

    public LoanBaseResponse createLoan(LoanRequest request) {
        if (request.duration() == 0) {
            throw new ValidationException("La durée doit être supérieure à 1");
        }

        if (request.amount().compareTo(BigDecimal.ZERO) == 0) {
            throw new ValidationException("Le montant doit être supérieur à 0");
        }

        Customer customer = this.customerRepository.findBytelephoneCustomer(request.telephone())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Account account = this.accountRepository.findBynumeroAccount(request.accountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        Loan loan = Loan.builder()
                .customer(customer)
                .account(account)
                .amount(request.amount())
                .reason(request.reason())
                .duration(request.duration())
                .loanType(LoanType.valueOf(request.type()))
                .loanStatus(LoanStatus.DRAFT)
                .build();

        loan = this.loanRepository.save(loan);
        return LoanBaseResponse.of(loan);
    }

    public void addDocument(long idLoan, DocumentRequest request) {
        Loan loan = this.loanRepository.findById(idLoan)
                .orElseThrow(() -> new ResourceNotFoundException("Ce crédit n'existe pas"));

        loan.getDocuments().forEach(loanDocument -> {
            if (loanDocument.getDocumentType().toString().equals(request.kycType())) {
                throw new ValidationException(
                        String.format("Le document de type %s a déjà été ajouté", request.kycType())
                );
            }
        });

        LoanDocument loanDocument = LoanDocument.builder()
                .loan(loan)
                .documentType(DocumentType.valueOf(request.kycType()))
                .urlDocument(request.fileUrl())
                .build();

        this.loanDocumentRepository.save(loanDocument);
    }
}
