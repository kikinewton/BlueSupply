package com.logistics.supply.dto.converter;

import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.dto.EmployeeMinorDTO;
import com.logistics.supply.dto.GrnMinorDTO;
import com.logistics.supply.model.GoodsReceivedNote;
import com.logistics.supply.model.GoodsReceivedNoteComment;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.logistics.supply.interfaces.GenericConverter;

@Component
public class GoodsReceivedNoteCommentConverter
    implements GenericConverter<GoodsReceivedNoteComment, CommentResponse<GrnMinorDTO>> {

  @Override
  public CommentResponse<GrnMinorDTO> apply(GoodsReceivedNoteComment goodsReceivedNoteComment) {
    CommentResponse<GrnMinorDTO> commentResponse = new CommentResponse<>();
    BeanUtils.copyProperties(goodsReceivedNoteComment, commentResponse);
    GoodsReceivedNote goodsReceivedNote = goodsReceivedNoteComment.getGoodsReceivedNote();
    GrnMinorDTO grnMinorDTO = GrnMinorDTO.toDto(goodsReceivedNote);
    EmployeeMinorDTO employeeMinorDTO =
        EmployeeMinorDTO.toDto(goodsReceivedNoteComment.getEmployee());
    commentResponse.setCommentBy(employeeMinorDTO);
    commentResponse.setItem(grnMinorDTO);
    return commentResponse;
  }
}
