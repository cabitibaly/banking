package com.jiyuu.banking.service;

import com.jiyuu.banking.dto.CustomerRequest;
import com.jiyuu.banking.dto.CustomerResponse;
import com.jiyuu.banking.entity.Customer;
import com.jiyuu.banking.entity.User;
import com.jiyuu.banking.enums.StatusCustomer;
import com.jiyuu.banking.exception.ValidationException;
import com.jiyuu.banking.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

@Service
@AllArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerResponse createCustomer(User user, CustomerRequest customerRequest) {

        int age = this.calculateAge(LocalDate.from(customerRequest.dateNaissance()));
        if (age < 18) {
            throw new ValidationException("Vous devez avoir au moins 18 ans pour créer un compte");
        }

        Customer customer = new Customer();

        customer.setUser(user);
        customer.setStatusCustomer(StatusCustomer.PENDING);
        customer.setNomCustomer(customerRequest.nom());
        customer.setPrenomCustomer(customerRequest.prenom());
        customer.setTelephoneCustomer(customerRequest.telephone());
        customer.setDateNaissance(customerRequest.dateNaissance());
        customer.setNumeroCustomer(this.generateNumeroCustomer(user.getIdUser()));

        this.customerRepository.save(customer);

        return new CustomerResponse(
                customer.getIdCustomer(),
                customer.getNumeroCustomer(),
                customer.getNomCustomer(),
                customer.getPrenomCustomer(),
                user.getEmail(),
                customer.getTelephoneCustomer(),
                customer.getDateNaissance(),
                customer.getStatusCustomer().toString(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    private String generateNumeroCustomer(long id) {
        String year = String.valueOf(java.time.Year.now().getValue());
        return String.format("CLI-%s-%04d", year, id);
    }

    private int calculateAge(LocalDate dateNaissance) {
        return Period.between(dateNaissance, LocalDate.now()).getYears();
    }
}
