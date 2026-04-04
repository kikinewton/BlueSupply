package com.logistics.supply.dto;


import lombok.Getter;

import java.util.List;

@Getter
public class BulkCommentDto {
    List<CommentRequest> comments;


    @Getter
    public static class CommentRequest {
        /**
         * the procurementTypeId stands for the id for the comment type supplied
         * example for LPO, represents request item id
         */
        Integer procurementTypeId;
        CommentDto comment;

    }
}

