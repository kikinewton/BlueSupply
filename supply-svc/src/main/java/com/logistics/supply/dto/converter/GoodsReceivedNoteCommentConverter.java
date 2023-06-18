package com.logistics.supply.dto.converter;

import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.dto.EmployeeMinorDto;
import com.logistics.supply.dto.GrnMinorDto;
import com.logistics.supply.model.GoodsReceivedNote;
import com.logistics.supply.model.GoodsReceivedNoteComment;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.logistics.supply.interfaces.GenericConverter;

@Component
public class GoodsReceivedNoteCommentConverter
    implements GenericConverter<GoodsReceivedNoteComment, CommentResponse<GrnMinorDto>> {

  @Override
  public CommentResponse<GrnMinorDto> apply(GoodsReceivedNoteComment goodsReceivedNoteComment) {
    CommentResponse<GrnMinorDto> commentResponse = new CommentResponse<>();
    BeanUtils.copyProperties(goodsReceivedNoteComment, commentResponse);
    GoodsReceivedNote goodsReceivedNote = goodsReceivedNoteComment.getGoodsReceivedNote();
    GrnMinorDto grnMinorDTO = GrnMinorDto.toDto(goodsReceivedNote);
    EmployeeMinorDto employeeMinorDTO =
        EmployeeMinorDto.toDto(goodsReceivedNoteComment.getEmployee());
    commentResponse.setCommentBy(employeeMinorDTO);
    commentResponse.setItem(grnMinorDTO);
    return commentResponse;
  }
}
