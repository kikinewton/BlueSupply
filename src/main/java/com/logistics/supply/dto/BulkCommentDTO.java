package com.logistics.supply.dto;


import com.logistics.supply.enums.ProcurementType;
import lombok.Getter;

import java.util.List;

@Getter
public class BulkCommentDTO {
    List<CommentRequest> comments;


    @Getter
    public static class CommentRequest {
        int procurementTypeId;
        CommentDTO comment;
        Boolean cancelled;
    }
}

