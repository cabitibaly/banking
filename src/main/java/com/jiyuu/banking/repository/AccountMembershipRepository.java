package com.jiyuu.banking.repository;

import com.jiyuu.banking.entity.AccountMembership;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountMembershipRepository extends JpaRepository<AccountMembership, Long> {
}
