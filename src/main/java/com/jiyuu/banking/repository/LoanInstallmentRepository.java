package com.jiyuu.banking.repository;

import com.jiyuu.banking.entity.LoanInstallment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LoanInstallmentRepository extends JpaRepository<LoanInstallment, Long>, JpaSpecificationExecutor<LoanInstallment> {

    Page<LoanInstallment> findAllByLoan_IdLoan(Long idLoan, Pageable pageable);
}
