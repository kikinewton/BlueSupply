package com.logistics.supply.fixture;

import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.PriorityLevel;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestReason;
import com.logistics.supply.enums.RequestReview;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.enums.RequestType;
import com.logistics.supply.model.RequestItem;
import java.math.BigDecimal;
import java.util.Date;

public class RequestItemFixture {

    private RequestItemFixture() {}

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        // mandatory fields — always valid defaults
        private String        name          = "Test Item";
        private RequestReason reason        = RequestReason.FreshNeed;
        private String        purpose       = "Test purpose";
        private int           quantity      = 1;
        private RequestType   requestType   = RequestType.GOODS_REQUEST;
        private PriorityLevel priorityLevel = PriorityLevel.NORMAL;
        // state fields
        private RequestStatus     status            = RequestStatus.PENDING;
        private EndorsementStatus endorsement       = EndorsementStatus.PENDING;
        private RequestApproval   approval          = RequestApproval.PENDING;
        private Date              endorsementDate   = null;
        private Date              approvalDate      = null;
        private RequestReview     requestReview     = null;
        private Date              requestReviewDate = null;
        // pricing fields — populated once the item is processed
        private BigDecimal        unitPrice         = BigDecimal.ZERO;
        private BigDecimal        totalPrice        = BigDecimal.ZERO;
        private String            currency          = null;
        private Integer           suppliedBy        = null;

        private Builder() {}

        public Builder processed() {
            this.status = RequestStatus.PROCESSED;
            this.unitPrice = BigDecimal.valueOf(100.00);
            this.totalPrice = BigDecimal.valueOf(100.00).multiply(BigDecimal.valueOf(quantity));
            this.currency = "GHS";
            this.suppliedBy = 1;
            return this;
        }

        public Builder endorsed() {
            this.endorsement = EndorsementStatus.ENDORSED;
            this.endorsementDate = new Date();
            return this;
        }

        public Builder approved() {
            this.approval = RequestApproval.APPROVED;
            this.approvalDate = new Date();
            return this;
        }

        public Builder reviewed(RequestReview review) {
            this.requestReview = review;
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
