package com.jiyuu.banking;

import com.jiyuu.banking.audit.context.AuditContext;
import com.jiyuu.banking.dto.CustomerRequest;
import com.jiyuu.banking.dto.CustomerResponse;
import com.jiyuu.banking.entity.Customer;
import com.jiyuu.banking.entity.KycDocument;
import com.jiyuu.banking.entity.Role;
import com.jiyuu.banking.entity.User;
import com.jiyuu.banking.enums.KycStatus;
import com.jiyuu.banking.enums.KycType;
import com.jiyuu.banking.enums.StatusCustomer;
import com.jiyuu.banking.enums.TypeOfRole;
import com.jiyuu.banking.exception.ResourceNotFoundException;
import com.jiyuu.banking.exception.ValidationException;
import com.jiyuu.banking.repository.CustomerRepository;
import com.jiyuu.banking.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {
    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    public void shouldThrowNullPointerExceptionWhenDateIsNull() {
        User user = this.buildUser();
        CustomerRequest request = new CustomerRequest(
                "Kyotaka Ayanokoji",
                "66583205",
                null
        );

        assertThrows(
                NullPointerException.class,
                () -> customerService.createCustomer(user, request)
        );
    }

    @Test
    public void shouldThrowsInvalidAgeOnCreateCustomer() {
        User user = this.buildUser();
        CustomerRequest request = new CustomerRequest(
                "Kyotaka Ayanokoji",
                "66583205",
                LocalDateTime.of(2010, 6, 22, 0, 0)
        );

        assertThrows(
                ValidationException.class,
                () -> customerService.createCustomer(user, request)
        );
    }

    @Test
    public void shouldThrowsTelephoneExistOnCreateCustomer() {
        User user = this.buildUser();
        Customer customer = new Customer();
        CustomerRequest request = new CustomerRequest(
                "Kyotaka Ayanokoji",
                "66583205",
                LocalDateTime.of(2000, 6, 22, 0, 0)
        );

        when(customerRepository.findBytelephoneCustomer("66583205"))
                .thenReturn(Optional.of(customer));

        assertThrows(
                ValidationException.class,
                () -> customerService.createCustomer(user, request)
        );
    }

    @Test
    public void shouldReturnCustomerResponseOnCreateCustomer() {
        User user = this.buildUser();
        Customer customer = this.buildCustomer(user, "PENDING");
        CustomerRequest request = new CustomerRequest(
                "Kyotaka Ayanokoji",
                "66583205",
                LocalDateTime.of(2000, 6, 22, 0, 0)
        );

        when(customerRepository.findBytelephoneCustomer("66583205"))
                .thenReturn(Optional.empty());

        when(customerRepository.save(any(Customer.class)))
                .thenReturn(customer);

        CustomerResponse response = customerService.createCustomer(user, request);

        assertNotNull(response);
        assertEquals(customer.getNumeroCustomer(), response.numero());
        assertEquals(StatusCustomer.PENDING, customer.getStatusCustomer());
        verify(customerRepository).save(any(Customer.class));
        verify(customerRepository).findBytelephoneCustomer("66583205");
    }

    @Test
    public void shouldThrowsResourceNotFoundExceptionWhenCustomerNotFound() {
        when(customerRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> customerService.changeCustomerStatus(1L, "VERIFIED")
        );
    }

    @Test
    public void shouldThrowValidationExceptionWhenKycDocumentsIsEmpty() {
        User user = this.buildUser();
        Customer customer = this.buildCustomer(user, "PENDING");

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        assertThrows(
                ValidationException.class,
                () -> customerService.changeCustomerStatus(1L, "VERIFIED")
        );
    }

    @Test
    public void shouldThrowValidationExceptionWhenAllDocsAreNotVerified() {
        User user = this.buildUser();
        Customer customer = this.buildCustomerWithKyc(user, KycStatus.PENDING);

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        assertThrows(
                ValidationException.class,
                () -> customerService.changeCustomerStatus(1L, "VERIFIED")
        );
    }

    @Test
    public void shouldChangeCustomerStatusWhenEverythingIsCorrect() {
        User user = this.buildUser();
        Customer customer = this.buildCustomerWithKyc(user, KycStatus.VERIFIED);
        Customer customerSave = this.buildCustomerWithKyc(user, KycStatus.VERIFIED);
        customerSave.setStatusCustomer(StatusCustomer.VERIFIED);

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(customerRepository.save(customer))
                .thenReturn(customerSave);

        CustomerResponse oldValue = CustomerResponse.of(customer);
        customerService.changeCustomerStatus(1L, "VERIFIED");
        CustomerResponse newValue = CustomerResponse.of(customerSave);

        verify(customerRepository).save(customer);
        assertEquals(oldValue, AuditContext.getOldValue());
        assertEquals(newValue, AuditContext.getNewValue());
    }

    @Test
    public void shouldThrowResourceNotFoundExceptionWhenCustomerNotFound() {
        when(customerRepository.findById(1L))
                .thenReturn(Optional.empty());

        CustomerRequest request = new CustomerRequest(
                "Kyotaka Ayanokoji",
                "66583205",
                LocalDateTime.of(2000, 6, 22, 0, 0)
        );



        assertThrows(
                ResourceNotFoundException.class,
                () -> customerService.updateCustomer(1L, request)
        );
    }

    @Test
    public void shouldThrowValidationExceptionWhenTelephoneIsAlreadyUsed() {
        User user = this.buildUser();
        Customer customer = this.buildCustomer(user, "VERIFIED");

        CustomerRequest request = new CustomerRequest(
                "Kyotaka Ayanokoji",
                "61500768",
                LocalDateTime.of(2000, 6, 22, 0, 0)
        );

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(customerRepository.findBytelephoneCustomer("61500768"))
                .thenReturn(Optional.of(customer));

        assertThrows(
                ValidationException.class,
                () -> customerService.updateCustomer(1L, request)
        );
    }

    @Test
    public void shouldUpdateCustomerWhenTelephoneIsDifferentAndNotUsed() {
        User user = this.buildUser();
        Customer customer = this.buildCustomer(user, "VERIFIED");
        Customer customerSave = this.buildCustomer(user, "VERIFIED");

        CustomerRequest request = new CustomerRequest(
                "Kyotaka Ayanokoji",
                "61500768",
                LocalDateTime.of(2000, 6, 22, 0, 0)
        );

        customerSave.setNomCustomer(request.nom());
        customerSave.setTelephoneCustomer(request.telephone());
        customerSave.setDateNaissance(request.dateNaissance());

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(customerRepository.findBytelephoneCustomer("61500768"))
                .thenReturn(Optional.empty());

        when(customerRepository.save(any(Customer.class)))
                .thenReturn(customerSave);

        CustomerResponse oldValue = CustomerResponse.of(customer);
        customerService.updateCustomer(1L, request);
        CustomerResponse newValue = CustomerResponse.of(customerSave);

        verify(customerRepository).save(customer);
        assertEquals(oldValue, AuditContext.getOldValue());
        assertEquals(newValue, AuditContext.getNewValue());
    }

    @Test
    public void shouldUpdateCustomerWhenTelephoneIsNotDifferent() {
        User user = this.buildUser();
        Customer customer = this.buildCustomer(user, "VERIFIED");
        Customer customerSave = this.buildCustomer(user, "VERIFIED");

        CustomerRequest request = new CustomerRequest(
                "Kyotaka Ayanokoji",
                "66583205",
                LocalDateTime.of(2000, 6, 22, 0, 0)
        );

        customerSave.setNomCustomer(request.nom());
        customerSave.setTelephoneCustomer(request.telephone());
        customerSave.setDateNaissance(request.dateNaissance());

        when(customerRepository.save(any(Customer.class)))
                .thenReturn(customerSave);

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        CustomerResponse oldValue = CustomerResponse.of(customer);
        customerService.updateCustomer(1L, request);
        CustomerResponse newValue = CustomerResponse.of(customerSave);

        verify(customerRepository).save(customer);
        assertEquals(oldValue, AuditContext.getOldValue());
        assertEquals(newValue, AuditContext.getNewValue());
    }

    private User buildUser() {
        Role role = new Role(1L, TypeOfRole.valueOf("CUSTOMER"));
        return User.builder()
                .idUser(1L)
                .email("example@exalome.com")
                .enabled(true)
                .password("$2a$10$hashedvalue")
                .tokenVersion(1)
                .role(role)
                .customer(null)
                .build();
    }

    private Customer buildCustomer(User user, String status) {
        return Customer.builder()
                .idCustomer(1L)
                .dateNaissance(LocalDateTime.of(2000, 6, 22, 0, 0))
                .telephoneCustomer("66583205")
                .nomCustomer("Kyotaka Ayanokoji")
                .numeroCustomer("CLI12345678")
                .statusCustomer(StatusCustomer.valueOf(status))
                .user(user)
                .kycDocuments(new ArrayList<>())
                .build();
    }

    private Customer buildCustomerWithKyc(User user, KycStatus status) {
        List<KycDocument> kycDocuments = new ArrayList<>();

        KycDocument doc = KycDocument.builder()
                .idKycDocument(1L)
                .fileUrl("http://localhost:8080")
                .kycStatus(status)
                .kycType(KycType.CNIB)
                .build();

        kycDocuments.add(doc);

        return Customer.builder()
                .idCustomer(1L)
                .dateNaissance(LocalDateTime.of(2000, 6, 22, 0, 0))
                .telephoneCustomer("61500768")
                .nomCustomer("Kyotaka Ayanokoji")
                .numeroCustomer("CLI12345678")
                .statusCustomer(StatusCustomer.PENDING)
                .user(user)
                .kycDocuments(kycDocuments)
                .build();
    }
}
