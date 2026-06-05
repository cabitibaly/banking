package com.jiyuu.banking.service;

import com.jiyuu.banking.audit.annotation.Auditable;
import com.jiyuu.banking.dto.*;
import com.jiyuu.banking.entity.Customer;
import com.jiyuu.banking.entity.KycDocument;
import com.jiyuu.banking.entity.User;
import com.jiyuu.banking.enums.KycStatus;
import com.jiyuu.banking.enums.KycType;
import com.jiyuu.banking.enums.StatusCustomer;
import com.jiyuu.banking.exception.ResourceNotFoundException;
import com.jiyuu.banking.exception.ValidationException;
import com.jiyuu.banking.repository.CustomerRepository;
import com.jiyuu.banking.repository.KycDocumentRepository;
import com.jiyuu.banking.repository.specification.CustomerSpecification;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final KycDocumentRepository kycDocumentRepository;

    @Auditable(action = "CREATE", entity = "CUSTOMER")
    public CustomerResponse createCustomer(User user, CustomerRequest customerRequest) {

        int age = this.calculateAge(LocalDate.from(customerRequest.dateNaissance()));
        if (age < 18) {
            throw new ValidationException("Vous devez avoir au moins 18 ans pour créer un compte");
        }

        Optional<Customer> customerOptional = this.customerRepository.findBytelephoneCustomer(customerRequest.telephone());

        if (customerOptional.isPresent()) {
            throw new ValidationException("Ce numéro de téléphone est déjà utilisé");
        }

        Customer customer = new Customer();

        customer.setUser(user);
        customer.setStatusCustomer(StatusCustomer.PENDING);
        customer.setNomCustomer(customerRequest.nom());
        customer.setTelephoneCustomer(customerRequest.telephone());
        customer.setDateNaissance(customerRequest.dateNaissance());
        customer.setNumeroCustomer(this.generateNumeroCustomer(user.getIdUser()));

        this.customerRepository.save(customer);

        return CustomerResponse.of(customer);
    }

    private String generateNumeroCustomer(long id) {
        String year = String.valueOf(java.time.Year.now().getValue());
        return String.format("CLI-%s-%04d", year, id);
    }

    private int calculateAge(LocalDate dateNaissance) {
        return Period.between(dateNaissance, LocalDate.now()).getYears();
    }

    @Auditable(action = "READ", entity = "CUSTOMER")
    public PagedResponse<CustomerResponse> getCustomers(CustomerSearchCriteria customerSearchCriteria, int page, int size, String soortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(soortBy).descending()
                : Sort.by(soortBy).ascending();

        Specification<Customer> spec = CustomerSpecification.withFiler(customerSearchCriteria);

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CustomerResponse> customers = this.customerRepository
                .findAll(spec, pageable)
                .map(CustomerResponse::of);

        return PagedResponse.of(customers);
    }

    @Auditable(action = "READ", entity = "CUSTOMER")
    public CustomerWithKycDocumentResponse getCustomer(long id) {
        Customer customer = this.customerRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Ce clientn'existe pas")
        );

        return CustomerWithKycDocumentResponse.of(customer);
    }

    @Auditable(action = "UPDATE", entity = "CUSTOMER")
    public void changeCustomerStatus(long idCustomer, String status) {
        Customer customer = this.customerRepository.findById(idCustomer)
                .orElseThrow(() -> new ResourceNotFoundException("Ce client n'existe pas"));

        if (status.equalsIgnoreCase("VERIFIED")) {
            if (customer.getKycDocuments().isEmpty()) {
                throw new ValidationException("Vous devez ajouter un document KYC pour changer le statut du client");
            }

            customer.getKycDocuments().forEach(kycDocument -> {
                if (kycDocument.getKycStatus() != KycStatus.VERIFIED ) {
                    throw new ValidationException("Tous les documents KYC doivent être validés pour changer le statut du client");
                }
            });
        }

        customer.setStatusCustomer(StatusCustomer.valueOf(status));
        this.customerRepository.save(customer);
    }

    @Auditable(action = "UPDATE", entity = "CUSTOMER")
    public void updateCustomer(long idCustomer, CustomerRequest customerRequest) {
        Customer customer = this.customerRepository.findById(idCustomer)
                .orElseThrow(() -> new ResourceNotFoundException("Ce client n'existe pas"));

        customer.setNomCustomer(customerRequest.nom());
        customer.setTelephoneCustomer(customerRequest.telephone());
        customer.setDateNaissance(customerRequest.dateNaissance());

        this.customerRepository.save(customer);
    }

    @Auditable(action = "DELETE", entity = "CUSTOMER")
    public void deleteCustomer(long idCustomer) {
        Customer customer = this.customerRepository.findById(idCustomer).orElseThrow(
                () -> new ResourceNotFoundException("Ce client n'existe pas")
        );

        this.customerRepository.delete(customer);
    }

    @Auditable(action = "CREATE", entity = "KycDocument")
    public void addKycDocument(long idCustomer, DocumentRequest documentRequest) {
        Customer customer = this.customerRepository.findById(idCustomer)
                .orElseThrow(() -> new ResourceNotFoundException("Ce client n'existe pas"));

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
