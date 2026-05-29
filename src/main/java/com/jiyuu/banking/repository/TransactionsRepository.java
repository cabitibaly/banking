package com.jiyuu.banking.repository;

import com.jiyuu.banking.dto.TransactionResponse;
import com.jiyuu.banking.entity.Transactions;
import com.jiyuu.banking.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TransactionsRepository extends JpaRepository<Transactions, Long>, JpaSpecificationExecutor<Transactions> {
    Optional<Transactions> findByTransactionRef(String ref);

    @Modifying
    @Transactional
    @Query("UPDATE Transactions t SET t.transactionStatus = :status WHERE t.idTransaction = :id")
    void updateStatus(@Param("id") Long id, @Param("status") TransactionStatus status);

}
