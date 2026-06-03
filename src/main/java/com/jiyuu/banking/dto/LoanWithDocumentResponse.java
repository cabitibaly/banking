package com.jiyuu.banking.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.jiyuu.banking.entity.Loan;

import java.util.List;

public record LoanWithDocumentResponse(
        @JsonUnwrapped LoanBaseResponse loan,
        List<KycDocumentResponse> documents
) {
    public static LoanWithDocumentResponse of(Loan loan) {
        return new LoanWithDocumentResponse(
                LoanBaseResponse.of(loan),
                loan.getDocuments().stream().map(KycDocumentResponse::toLoanDocument).toList()
        );
    }
}
