package com.logistics.supply.fixture;

import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.PriorityLevel;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestReason;
import com.logistics.supply.enums.RequestReview;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.enums.RequestType;
import com.logistics.supply.model.RequestItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;

import java.math.BigDecimal;
import java.util.Date;

public class RequestItemFixture {

    private RequestItemFixture() {}

    public static RequestItemTestBuilder pending() {
        return new RequestItemTestBuilder();
    }

    public static RequestItemTestBuilder processed() {
        return new RequestItemTestBuilder().processed();
    }

    public static RequestItemTestBuilder endorsed() {
        return new RequestItemTestBuilder().endorsed();
    }

    public static RequestItemTestBuilder approved() {
        return new RequestItemTestBuilder().approved();
    }

    public static RequestItemTestBuilder hodReview() {
        return new RequestItemTestBuilder().endorsed().processed().hodReview();
    }

    public static RequestItemTestBuilder reviewed(RequestReview review) {
        return new RequestItemTestBuilder().reviewed(review);
    }

    @Getter
    @With
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestItemTestBuilder {
        private String            name              = "Test Item";
        private RequestReason     reason            = RequestReason.FreshNeed;
        private String            purpose           = "Test purpose";
        private int               quantity          = 1;
        private RequestType       requestType       = RequestType.GOODS_REQUEST;
        private PriorityLevel     priorityLevel     = PriorityLevel.NORMAL;
        private RequestStatus     status            = RequestStatus.PENDING;
        private EndorsementStatus endorsement       = EndorsementStatus.PENDING;
        private RequestApproval   approval          = RequestApproval.PENDING;
        private Date              endorsementDate   = null;
        private Date              approvalDate      = null;
        private RequestReview     requestReview     = null;
        private Date              requestReviewDate = null;
        private BigDecimal        unitPrice         = BigDecimal.ZERO;
        private BigDecimal        totalPrice        = BigDecimal.ZERO;
        private String            currency          = null;
        private Integer           suppliedBy        = null;

        public RequestItemTestBuilder processed() {
            this.status        = RequestStatus.PROCESSED;
            this.unitPrice     = BigDecimal.valueOf(100.00);
            this.totalPrice    = BigDecimal.valueOf(100.00).multiply(BigDecimal.valueOf(quantity));
            this.requestReview = RequestReview.PENDING;
            this.currency      = "GHS";
            this.suppliedBy    = 1;
            return this;
        }

        public RequestItemTestBuilder hodReview() {
            this.requestReview     = RequestReview.HOD_REVIEW;
            this.requestReviewDate = new Date();
            return this;
        }

        public RequestItemTestBuilder endorsed() {
            this.endorsement     = EndorsementStatus.ENDORSED;
            this.endorsementDate = new Date();
            return this;
        }

        public RequestItemTestBuilder approved() {
            this.approval     = RequestApproval.APPROVED;
            this.approvalDate = new Date();
            return this;
        }

        public RequestItemTestBuilder reviewed(RequestReview review) {
            this.requestReview     = review;
            this.requestReviewDate = new Date();
            return this;
        }

        public RequestItem build() {
            RequestItem item = new RequestItem();
            item.setName(name);
            item.setReason(reason);
            item.setPurpose(purpose);
            item.setQuantity(quantity);
            item.setRequestType(requestType);
            item.setPriorityLevel(priorityLevel);
            item.setStatus(status);
            item.setEndorsement(endorsement);
            item.setEndorsementDate(endorsementDate);
            item.setApproval(approval);
            item.setApprovalDate(approvalDate);
            item.setRequestReview(requestReview);
            item.setRequestReviewDate(requestReviewDate);
            item.setUnitPrice(unitPrice);
            item.setTotalPrice(totalPrice);
            item.setCurrency(currency);
            item.setSuppliedBy(suppliedBy);
            return item;
        }
    }
}