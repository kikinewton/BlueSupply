package com.logistics.supply.util;

import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.exception.PettyCashApprovalException;
import com.logistics.supply.exception.PettyCashEndorsementException;
import com.logistics.supply.exception.PettyCashFundsAllocationException;
import com.logistics.supply.model.PettyCash;
import lombok.NonNull;

public class PettyCashValidatorUtil {
    PettyCashValidatorUtil() {
    }

    public static void validateEndorsement(@NonNull PettyCash pettyCash) {
        if (pettyCash.getEndorsement() == EndorsementStatus.PENDING) {
            return;
        }
        throw new PettyCashEndorsementException(pettyCash.getId());
    }

    public static void validateApproval(@NonNull PettyCash pettyCash) {
        if (pettyCash.getEndorsement() == EndorsementStatus.ENDORSED && pettyCash.getApproval() == RequestApproval.PENDING) {
            return;
        }
        throw new PettyCashApprovalException(pettyCash.getId());
    }

    public static void validateFundsAllocation(@NonNull PettyCash pettyCash) {
        if (pettyCash.getApproval().equals(RequestApproval.APPROVED) && !pettyCash.isPaid()) {
            return;
        }
        throw new PettyCashFundsAllocationException(pettyCash.getId());
    }
}
