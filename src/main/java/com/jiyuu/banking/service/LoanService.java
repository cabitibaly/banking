package com.jiyuu.banking.service;

import com.jiyuu.banking.dto.*;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

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

        Set <LoanStatus> statuses = EnumSet.of(
                LoanStatus.APPROVED,
                LoanStatus.REJECTED,
                LoanStatus.ACTIVE,
                LoanStatus.CLOSED,
                LoanStatus.UNDER_REVIEW
        );

        if(statuses.contains(loan.getLoanStatus())) {
            throw new ValidationException("Ce crédit a dejà été traité ou est en cours de traitement");
        }

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

        if(loan.getLoanStatus() == LoanStatus.DRAFT) {
            loan.setLoanStatus(LoanStatus.PENDING);
        }


        this.loanDocumentRepository.save(loanDocument);
    }

    public PagedResponse<LoanBaseResponse> AllLoans(int page, int size, String soortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(soortBy).descending()
                : Sort.by(soortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<LoanBaseResponse> loans = this.loanRepository
                .findAll(pageable)
                .map(LoanBaseResponse::of);

        return PagedResponse.of(loans);
    }

    public LoanWithDocumentResponse getLoan(long id) {
        Loan loan = this.loanRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Ce crédit n'existe pas")
        );

        return LoanWithDocumentResponse.of(loan);
    }

    public void approveLoan(long id) {
        Loan loan = this.loanRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Ce crédit n'existe pas")
        );

        Set<LoanStatus> statuses = EnumSet.of(
                LoanStatus.APPROVED,
                LoanStatus.REJECTED,
                LoanStatus.ACTIVE,
                LoanStatus.CLOSED,
                LoanStatus.UNDER_REVIEW
        );

        if(statuses.contains(loan.getLoanStatus())) {
            throw new ValidationException("Ce crédit a dejà été traité.");
        }

        loan.setLoanStatus(LoanStatus.APPROVED);
        this.loanRepository.save(loan);
    }

    public void rejectLoan(long id) {
        Loan loan = this.loanRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Ce crédit n'existe pas")
        );

        Set<LoanStatus> statuses = EnumSet.of(
                LoanStatus.APPROVED,
                LoanStatus.REJECTED,
                LoanStatus.ACTIVE,
                LoanStatus.CLOSED,
                LoanStatus.UNDER_REVIEW
        );

        if(statuses.contains(loan.getLoanStatus())) {
            throw new ValidationException("Ce crédit a dejà été traité.");
        }

        loan.setLoanStatus(LoanStatus.REJECTED);
        this.loanRepository.save(loan);
    }
}
