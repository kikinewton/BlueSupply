package com.logistics.supply.interfaces;

import com.logistics.supply.model.Comment;

import java.util.List;

public interface ICommentService<T extends Comment> {

    T addComment(T comment);
    List<T> findUnReadComment(int employeeId);
    List<T> findByCommentTypeId(int id);



}
