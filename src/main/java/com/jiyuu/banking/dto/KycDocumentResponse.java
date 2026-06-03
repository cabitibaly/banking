package com.jiyuu.banking.dto;

import com.jiyuu.banking.entity.KycDocument;
import com.jiyuu.banking.entity.LoanDocument;

public record KycDocumentResponse(
        long id,
        String fileUrl,
        String kycType,
        String status
) {
    public static KycDocumentResponse of(KycDocument kycDocument) {
        return new KycDocumentResponse(
                kycDocument.getIdKycDocument(),
                kycDocument.getFileUrl(),
                kycDocument.getKycType().toString(),
                kycDocument.getKycStatus().toString()
        );
    }

    public static KycDocumentResponse toLoanDocument(LoanDocument loanDocument) {
        return new KycDocumentResponse(
                loanDocument.getIdLoanDocument(),
                loanDocument.getUrlDocument(),
                loanDocument.getDocumentType().toString(),
                null
        );
    }
}
