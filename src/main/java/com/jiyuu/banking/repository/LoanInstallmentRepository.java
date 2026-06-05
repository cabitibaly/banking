package com.jiyuu.banking.repository;

import com.jiyuu.banking.entity.Loan;
import com.jiyuu.banking.entity.LoanInstallment;
import com.jiyuu.banking.enums.InstallmentStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoanInstallmentRepository extends JpaRepository<LoanInstallment, Long>, JpaSpecificationExecutor<LoanInstallment> {

    Page<LoanInstallment> findAllByLoan_IdLoan(Long idLoan, Pageable pageable);

    @Query("""
        SELECT i FROM LoanInstallment i
        JOIN FETCH i.loan l
        JOIN FETCH l.account a
        WHERE i.dueDate = :dueDate
        AND i.installmentStatus = :status
    """)
    List<LoanInstallment> findByDueDateAndInstallmentStatus(
            @Param("dueDate") LocalDate dueDate,
            @Param("status") InstallmentStatus status
    );

    @Query("""
        SELECT i FROM LoanInstallment i        
        WHERE i.installmentStatus = "OVERDUE" OR i.installmentStatus = "PENDING"
        AND i.loan = :loan
    """)
    List<LoanInstallment> findByLoanOverdueOrPaid(@Param("loan")Loan loan);
}
