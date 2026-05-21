package com.jiyuu.banking.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.jiyuu.banking.entity.Customer;

import java.util.List;

public record CustomerWithKycDocumentResponse(
        @JsonUnwrapped CustomerResponse customer,
        List<KycDocumentResponse> kycDocuments
) {
    public static CustomerWithKycDocumentResponse of(Customer customer) {
        return new CustomerWithKycDocumentResponse(
                CustomerResponse.of(customer),
                customer.getKycDocuments().stream().map(KycDocumentResponse::of).toList()
        );
    }
}
