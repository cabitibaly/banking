package com.jiyuu.banking.repository;

import com.jiyuu.banking.entity.LoanDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanDocumentRepository extends JpaRepository<LoanDocument, Long> {
}
