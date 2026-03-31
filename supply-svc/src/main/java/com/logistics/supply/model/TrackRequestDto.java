package com.logistics.supply.model;

import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestReview;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestStatus;

import java.util.Date;

@Setter
@Getter
@ToString
@NoArgsConstructor
public class TrackRequestDto {

    private EndorsementStatus endorsement;
    private Date endorsementDate;
    private RequestApproval approval;
    private Date approvalDate;
    private RequestStatus status;
    private RequestReview requestReview;
    private Date requestReviewDate;
    private String lpoIssued;
    private Date lpoIssuedDate;
    private String grnIssued;
    private Date grnIssuedDate;
    private String grnHodEndorse;
    private Date grnHodEndorseDate;
    private String grnGmApprove;
    private String procurementAdvise;
    private Date procurementAdviseDate;
    private String paymentInitiated;
    private Date paymentInitiatedDate;
    private String paymentAuditorCheck;
    private Date paymentAuditorCheckDate;
    private String paymentFMAuthorise;
    private Date paymentFMAuthoriseDate;
    private String paymentGMApprove;
    private Date paymentGMApproveDate;

    public static TrackRequestDto fromRequestItem(RequestItem item) {
        TrackRequestDto dto = new TrackRequestDto();
        dto.endorsement = item.getEndorsement();
        dto.approval = item.getApproval();
        dto.status = item.getStatus();
        dto.requestReview = item.getRequestReview();
        dto.setEndorsementDate(item.getEndorsementDate());
        dto.setApprovalDate(item.getApprovalDate());
        dto.setRequestReviewDate(item.getRequestReviewDate());
        return dto;
    }

}
