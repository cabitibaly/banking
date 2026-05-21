package com.jiyuu.banking.dto;

import com.jiyuu.banking.entity.KycDocument;

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
}
