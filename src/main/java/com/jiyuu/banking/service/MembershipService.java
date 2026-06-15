package com.jiyuu.banking.service;

import com.jiyuu.banking.audit.annotation.Auditable;
import com.jiyuu.banking.audit.context.AuditContext;
import com.jiyuu.banking.dto.MemebershipResponse;
import com.jiyuu.banking.entity.Account;
import com.jiyuu.banking.entity.AccountMembership;
import com.jiyuu.banking.entity.Customer;
import com.jiyuu.banking.exception.AccessDeniedException;
import com.jiyuu.banking.exception.DuplicateResourceException;
import com.jiyuu.banking.exception.ResourceNotFoundException;
import com.jiyuu.banking.repository.AccountMembershipRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class MembershipService {
    private final AccountMembershipRepository membershipRepository;
    private final AccountService accountService;
    private final CustomerService customerService;

    @Auditable(action = "UPDATE", entity = "ACCOUNTMEMBERSHIP")
    public void addNewMember(long idAccount, long idCustomer) {
        Optional<AccountMembership> membershipOptional = this.membershipRepository
                .findByAccount_idAccountAndCustomer_IdCustomer(idAccount, idCustomer);

        if (membershipOptional.isPresent()) {
            throw new DuplicateResourceException("Ce client est déja associé à ce compte");
        }

        Account account = this.accountService.getAccountEntity(idAccount);
        Customer customer = this.customerService.getCustomerEntity(idCustomer);

        AccountMembership accountMembership = AccountMembership.builder()
                .isPrimary(false)
                .account(account)
                .customer(customer)
                .build();

        accountMembership = this.membershipRepository.save(accountMembership);

        MemebershipResponse response = MemebershipResponse.of(
                account,
                customer,
                accountMembership.isPrimary(),
                accountMembership.getDateAdhesion()
        );

        AuditContext.setOldValue(response);
    }

    @Auditable(action = "DELETE", entity = "ACCOUNTMEMBERSHIP")
    public void deleteMember(long idAccount, long idCustomer) {
        AccountMembership accountMembership = this.membershipRepository.findByAccount_idAccountAndCustomer_IdCustomer(idAccount, idCustomer)
                .orElseThrow(() -> new ResourceNotFoundException("AccountMembership not found"));

        if (accountMembership.isPrimary()) {
            throw new AccessDeniedException("Impossible de supprimer le membre principal");
        }

        MemebershipResponse response = MemebershipResponse.of(
                accountMembership.getAccount(),
                accountMembership.getCustomer(),
                accountMembership.isPrimary(),
                accountMembership.getDateAdhesion()
        );

        AuditContext.setOldValue(response);

        this.membershipRepository.delete(accountMembership);
    }
}
