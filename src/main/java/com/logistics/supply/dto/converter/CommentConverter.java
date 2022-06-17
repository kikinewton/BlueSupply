package com.logistics.supply.dto.converter;

import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.interfaces.GenericConverter;
import com.logistics.supply.model.RequestItemComment;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommentConverter
    implements GenericConverter<RequestItemComment, CommentResponse<RequestItemComment>> {

  @Override
  public CommentResponse<RequestItemComment> apply(RequestItemComment requestItemComment) {
    CommentResponse<RequestItemComment> commentResponse = new CommentResponse<>();
    BeanUtils.copyProperties(requestItemComment, commentResponse);
    commentResponse.setItem(requestItemComment);
    return commentResponse;
  }


  //  public static <T> Collection<CommentResponse<T>> toDto(Class<T> tClass, Collection<T> comment)
  // {
  //    List<CommentResponse<T>> tCollection = new ArrayList();
  //    comment.forEach(
  //        c -> {
  //          CommentResponse<T> commentResponse = new CommentResponse();
  //          BeanUtils.copyProperties(c, commentResponse);
  //          tCollection.add(commentResponse);
  //        });
  //    return tCollection;
  //  }

}
