package com.logistics.supply.interfaces;

import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.dto.MinorDTO;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.Comment;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public interface ICommentService<T extends Comment, O extends MinorDTO> {

    T addComment(T comment) throws GeneralException;
    List<CommentResponse<O>> findByCommentTypeId(int id);
    ByteArrayInputStream getCommentDataSheet(int id) throws IOException;



}
