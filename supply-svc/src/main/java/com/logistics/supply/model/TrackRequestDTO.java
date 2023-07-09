package com.logistics.supply.model;

import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestReview;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestStatus;

@Setter
@Getter
@ToString
@NoArgsConstructor
public class TrackRequestDTO {

    private EndorsementStatus endorsement;
    private RequestApproval approval;
    private RequestStatus status;
    private RequestReview requestReview;
    private String lpoIssued;
    private String grnIssued;
    private String grnHodEndorse;
    private String grnGmApprove;
    private String procurementAdvise;
    private String paymentInitiated;
    private String paymentAuditorCheck;
    private String paymentFMAuthorise;
    private String paymentGMApprove;

}
