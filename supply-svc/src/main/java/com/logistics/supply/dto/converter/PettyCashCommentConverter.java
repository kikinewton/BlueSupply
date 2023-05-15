package com.logistics.supply.dto.converter;

import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.dto.EmployeeMinorDTO;
import com.logistics.supply.model.PettyCash;
import com.logistics.supply.model.PettyCashComment;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.logistics.supply.interfaces.GenericConverter;

@Component
public class PettyCashCommentConverter implements GenericConverter<PettyCashComment, CommentResponse<PettyCash.PettyCashMinorDTO>> {
    @Override
    public CommentResponse<PettyCash.PettyCashMinorDTO> apply(PettyCashComment pettyCashComment) {
        CommentResponse<PettyCash.PettyCashMinorDTO> commentResponse = new CommentResponse<>();
        EmployeeMinorDTO employeeMinorDTO = EmployeeMinorDTO.toDto(pettyCashComment.getEmployee());
        BeanUtils.copyProperties(pettyCashComment, commentResponse);
        PettyCash.PettyCashMinorDTO pettyCashMinorDTO = PettyCash.PettyCashMinorDTO.toDto(pettyCashComment.getPettyCash());
        commentResponse.setItem(pettyCashMinorDTO);
        commentResponse.setCommentBy(employeeMinorDTO);
        return commentResponse;
    }
}
