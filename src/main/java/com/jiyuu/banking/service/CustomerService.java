package com.jiyuu.banking.service;

import com.jiyuu.banking.audit.annotation.Auditable;
import com.jiyuu.banking.dto.*;
import com.jiyuu.banking.entity.Customer;
import com.jiyuu.banking.entity.User;
import com.jiyuu.banking.enums.KycStatus;
import com.jiyuu.banking.enums.StatusCustomer;
import com.jiyuu.banking.exception.ResourceNotFoundException;
import com.jiyuu.banking.exception.ValidationException;
import com.jiyuu.banking.repository.CustomerRepository;
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

    public Customer getCustomerEntity(long id) {
        return this.customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ce clientn'existe pas"));
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
}
