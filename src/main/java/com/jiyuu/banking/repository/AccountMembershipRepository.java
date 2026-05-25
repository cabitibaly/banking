package com.jiyuu.banking.repository;

import com.jiyuu.banking.entity.AccountMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AccountMembershipRepository extends JpaRepository<AccountMembership, Long> {

    Optional<AccountMembership> findByAccount_idAccountAndCustomer_IdCustomer(long idAccount, long idCustomer);

    List<AccountMembership> findByAccount_idAccount(long idCustomer);
}
