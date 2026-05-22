package com.jiyuu.banking.service;

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
import com.jiyuu.banking.repository.AccountMembershipRepository;
import com.jiyuu.banking.repository.AccountRepository;
import com.jiyuu.banking.repository.CustomerRepository;
import com.jiyuu.banking.repository.specification.AccountSpecification;
import com.jiyuu.banking.utils.AccountNumberGenerator;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountMembershipRepository accountMembershipRepository;
    private final CustomerRepository customerRepository;

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
                .estDecouvert(false)
                .build();

        account = this.accountRepository.save(account);

        AccountMembership accountMembership = AccountMembership.builder()
                .isPrimary(true)
                .account(account)
                .customer(customer)
                .build();

        this.accountMembershipRepository.save(accountMembership);
    }

    public void changeAccountStatus(long idAccount, String status) {
        Account account = this.accountRepository.findById(idAccount)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        account.setAccountStatus(AccountStatus.valueOf(status));
        this.accountRepository.save(account);
    }

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

    public void deleteMember(long idAccount, long idCustomer) {
        AccountMembership accountMembership = this.accountMembershipRepository.findByAccount_idAccountAndCustomer_IdCustomer(idAccount, idCustomer)
                .orElseThrow(() -> new ResourceNotFoundException("AccountMembership not found"));

        if (accountMembership.isPrimary()) {
            throw new AccessDeniedException("Impossible de supprimer le membre principal");
        }

        this.accountMembershipRepository.delete(accountMembership);
    }

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

    public AccountResponse getAccount(long id) {
        Account account = this.accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        return AccountResponse.of(account);
    }

}
