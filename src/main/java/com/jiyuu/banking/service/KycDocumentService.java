package com.jiyuu.banking.service;

import com.jiyuu.banking.audit.annotation.Auditable;
import com.jiyuu.banking.dto.DocumentRequest;
import com.jiyuu.banking.entity.Customer;
import com.jiyuu.banking.entity.KycDocument;
import com.jiyuu.banking.enums.KycStatus;
import com.jiyuu.banking.enums.KycType;
import com.jiyuu.banking.exception.ResourceNotFoundException;
import com.jiyuu.banking.exception.ValidationException;
import com.jiyuu.banking.repository.KycDocumentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class KycDocumentService {
    private final KycDocumentRepository kycDocumentRepository;
    private final CustomerService customerService;

    @Auditable(action = "CREATE", entity = "KycDocument")
    public void addKycDocument(long idCustomer, DocumentRequest documentRequest) {
        Customer customer = this.customerService.getCustomerEntity(idCustomer);

        customer.getKycDocuments().forEach(kycDocument -> {
            if (kycDocument.getKycType().toString().equals(documentRequest.kycType())) {
                throw new ValidationException(
                        String.format("Le document KYC de type %s a déjà été ajouté", documentRequest.kycType())
                );
            }
        });

        KycDocument kycDocument = new KycDocument();
        kycDocument.setCustomer(customer);
        kycDocument.setFileUrl(documentRequest.fileUrl());
        kycDocument.setKycStatus(KycStatus.PENDING);
        kycDocument.setKycType(KycType.valueOf(documentRequest.kycType()));

        this.kycDocumentRepository.save(kycDocument);
    }

    @Auditable(action = "UPDATE", entity = "KycDocument")
    public void updateKycDocument(long idKycDocument, long idCustomer, String status) {
        KycDocument kycDocument = this.kycDocumentRepository.findByIdKycDocumentAndCustomer_IdCustomer(idKycDocument, idCustomer)
                .orElseThrow(() -> new ResourceNotFoundException("Ce client n'a pas de KYC document"));

        if (kycDocument.getKycStatus() != KycStatus.PENDING && kycDocument.getKycStatus() != KycStatus.IN_REVIEW) {
            throw new ValidationException("Le document KYC a déjà été traité");
        }

        kycDocument.setKycStatus(KycStatus.valueOf(status));
        this.kycDocumentRepository.save(kycDocument);
    }

    @Auditable(action = "DELETE", entity = "KycDocument")
    public void deleteKycDocument(long idKycDocument, long idCustomer) {
        KycDocument kycDocument = this.kycDocumentRepository.findByIdKycDocumentAndCustomer_IdCustomer(idKycDocument, idCustomer)
                .orElseThrow(() -> new ResourceNotFoundException("Ce client n'a pas de KYC document"));

        if (kycDocument.getKycStatus() == KycStatus.IN_REVIEW) {
            throw new ValidationException("Le document KYC est en cours de traitement");
        }

        this.kycDocumentRepository.delete(kycDocument);
    }
}
