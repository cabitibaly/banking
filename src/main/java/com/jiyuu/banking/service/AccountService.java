package com.jiyuu.banking.service;

import com.jiyuu.banking.audit.annotation.Auditable;
import com.jiyuu.banking.dto.AccountRequest;
import com.jiyuu.banking.dto.AccountResponse;
import com.jiyuu.banking.dto.AccountSearchCriteria;
import com.jiyuu.banking.dto.PagedResponse;
import com.jiyuu.banking.entity.Account;
import com.jiyuu.banking.entity.AccountMembership;
import com.jiyuu.banking.entity.Customer;
import com.jiyuu.banking.enums.AccountStatus;
import com.jiyuu.banking.enums.AccountType;
import com.jiyuu.banking.enums.Currency;
import com.jiyuu.banking.exception.AccessDeniedException;
import com.jiyuu.banking.exception.ResourceNotFoundException;
import com.jiyuu.banking.exception.ValidationException;
import com.jiyuu.banking.repository.AccountMembershipRepository;
import com.jiyuu.banking.repository.AccountRepository;
import com.jiyuu.banking.repository.CustomerRepository;
import com.jiyuu.banking.repository.specification.AccountSpecification;
import com.jiyuu.banking.utils.AccountNumberGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountMembershipRepository accountMembershipRepository;
    private final CustomerRepository customerRepository;

    @Value("${max-decouvert}")
    private long MAX_DECOUVERT;

    public AccountService(AccountRepository accountRepository, AccountMembershipRepository accountMembershipRepository, CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.accountMembershipRepository = accountMembershipRepository;
        this.customerRepository = customerRepository;
    }

//    @Auditable(action = "CREATE", entity = "ACCOUNT, ACCOUNTMEMBERSHIP")
    public void createAccount(AccountRequest accountRequest) {
        Customer customer = this.customerRepository.findById(accountRequest.idCustomer())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        String accountNumber = AccountNumberGenerator.generate();
        Account account = Account.builder()
                .accountType(AccountType.valueOf(accountRequest.type()))
                .accountStatus(AccountStatus.PENDING)
                .numeroAccount(accountNumber)
                .currencyAccount(Currency.valueOf(accountRequest.currency()))
                .soldeAccount(BigDecimal.ZERO)
                .decouvert(accountRequest.decouvert())
                .build();

        account = this.accountRepository.save(account);

        AccountMembership accountMembership = AccountMembership.builder()
                .isPrimary(true)
                .account(account)
                .customer(customer)
                .build();

        this.accountMembershipRepository.save(accountMembership);
    }

    @Auditable(action = "UPDATE", entity = "ACCOUNT")
    public void changeAccountStatus(long idAccount, String status) {
        Account account = this.accountRepository.findById(idAccount)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        account.setAccountStatus(AccountStatus.valueOf(status));
        this.accountRepository.save(account);
    }

    @Auditable(action = "UPDATE", entity = "ACCOUNT")
    public void changeDecouvert(long idAccount, BigDecimal decouvert) {
        Account account = this.accountRepository.findById(idAccount)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if(decouvert.compareTo(BigDecimal.valueOf(MAX_DECOUVERT)) > 0) {
            throw new ValidationException("Le montant de découverte ne peut pas dépasser le maximum de " + MAX_DECOUVERT);
        }

        account.setDecouvert(decouvert);
        this.accountRepository.save(account);
    }

    @Auditable(action = "CREATE", entity = "ACCOUNTMEMBERSHIP")
    public void addNewMember(long idAccount, long idCustomer) {
        Account account = this.accountRepository.findById(idAccount)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        Customer customer = this.customerRepository.findById(idCustomer)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        AccountMembership accountMembership = AccountMembership.builder()
                .isPrimary(false)
                .account(account)
                .customer(customer)
                .build();

        this.accountMembershipRepository.save(accountMembership);
    }

    @Auditable(action = "DELETE", entity = "ACCOUNTMEMBERSHIP")
    public void deleteMember(long idAccount, long idCustomer) {
        AccountMembership accountMembership = this.accountMembershipRepository.findByAccount_idAccountAndCustomer_IdCustomer(idAccount, idCustomer)
                .orElseThrow(() -> new ResourceNotFoundException("AccountMembership not found"));

        if (accountMembership.isPrimary()) {
            throw new AccessDeniedException("Impossible de supprimer le membre principal");
        }

        this.accountMembershipRepository.delete(accountMembership);
    }

    @Auditable(action = "READ", entity = "ACCOUNT")
    public PagedResponse<AccountResponse> getAccounts(AccountSearchCriteria accountSearchCriteria, int page, int size, String soortBy, String direction) {
        Specification<Account> spec = AccountSpecification.withFiler(accountSearchCriteria);

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(soortBy).descending()
                : Sort.by(soortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<AccountResponse> accounts = this.accountRepository
                .findAll(spec, pageable)
                .map(AccountResponse::of);

        return PagedResponse.of(accounts);
    }

    @Auditable(action = "READ", entity = "ACCOUNT")
    public AccountResponse getAccount(long id) {
        Account account = this.accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        return AccountResponse.of(account);
    }

}
