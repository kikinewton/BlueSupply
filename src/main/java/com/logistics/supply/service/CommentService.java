package com.logistics.supply.service;

import com.logistics.supply.model.Comment;
import com.logistics.supply.repository.CommentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CommentService {

  @Autowired CommentRepository commentRepository;

  public Comment saveComment(Comment comment) {
    try {
      return commentRepository.save(comment);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public Comment findById(int commentId) {
    return commentRepository.findById(commentId).orElse(null);
  }
}
