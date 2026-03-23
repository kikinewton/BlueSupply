package com.logistics.supply.model;

import lombok.Getter;
import org.springframework.data.annotation.Immutable;

import jakarta.persistence.*;

@Entity
@Getter
@Immutable
@Table(name = "procurement_funnel_view")
public class ProcurementFunnel {

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "pending_endorsement")
    private long pendingEndorsement;

    @Column(name = "endorsed_pending_processing")
    private long endorsedPendingProcessing;

    @Column(name = "pending_hod_review")
    private long pendingHodReview;

    @Column(name = "lpo_drafts_awaiting_approval")
    private long lpoDraftsAwaitingApproval;

    @Column(name = "approved_lpos_without_grn")
    private long approvedLposWithoutGrn;
}
