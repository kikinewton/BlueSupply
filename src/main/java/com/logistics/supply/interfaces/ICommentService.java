package com.logistics.supply.interfaces;

import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.dto.MinorDTO;
import com.logistics.supply.model.Comment;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface ICommentService<T extends Comment, O extends MinorDTO> {

    T addComment(T comment);
    List<CommentResponse<O>> findByCommentTypeId(int id);
    ByteArrayInputStream getCommentDataSheet(int id);



}
