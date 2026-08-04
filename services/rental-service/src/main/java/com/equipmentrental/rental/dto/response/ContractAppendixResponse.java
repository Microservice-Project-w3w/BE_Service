package com.equipmentrental.rental.dto.response;

import com.equipmentrental.rental.entity.AppendixStatus;
import com.equipmentrental.rental.entity.AppendixType;
import java.time.LocalDateTime;

public record ContractAppendixResponse(Long id, Long organizationId, Long branchId, Long contractId,
                                       String appendixCode, AppendixType appendixType, AppendixStatus status,
                                       LocalDateTime newEndAt, String terms, LocalDateTime approvedAt, LocalDateTime signedAt) {
}
