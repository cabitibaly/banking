package com.jiyuu.banking.repository;

import com.jiyuu.banking.entity.KycDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface KycDocumentRepository extends JpaRepository<KycDocument, Long> {

    Optional<KycDocument> findByIdKycDocumentAndCustomer_IdCustomer(Long idKycDocument, Long idCustomer);
}
