package com.logistics.supply.dto;

public record PendingApprovalsDTO(
        long pendingEndorsement,
        long lpoDraftsAwaitingApproval
) {}
