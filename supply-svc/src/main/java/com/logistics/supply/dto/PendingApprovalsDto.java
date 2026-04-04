package com.logistics.supply.dto;

public record PendingApprovalsDto(
        long pendingEndorsement,
        long lpoDraftsAwaitingApproval
) {}
