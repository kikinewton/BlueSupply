package com.logistics.supply.dto.converter;

import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.dto.EmployeeMinorDto;
import com.logistics.supply.model.PettyCash;
import com.logistics.supply.model.PettyCashComment;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.logistics.supply.interfaces.GenericConverter;

@Component
public class PettyCashCommentConverter implements GenericConverter<PettyCashComment, CommentResponse<PettyCash.PettyCashMinorDto>> {
    @Override
    public CommentResponse<PettyCash.PettyCashMinorDto> apply(PettyCashComment pettyCashComment) {
        CommentResponse<PettyCash.PettyCashMinorDto> commentResponse = new CommentResponse<>();
        EmployeeMinorDto employeeMinorDTO = EmployeeMinorDto.toDto(pettyCashComment.getEmployee());
        BeanUtils.copyProperties(pettyCashComment, commentResponse);
        PettyCash.PettyCashMinorDto pettyCashMinorDTO = PettyCash.PettyCashMinorDto.toDto(pettyCashComment.getPettyCash());
        commentResponse.setItem(pettyCashMinorDTO);
        commentResponse.setCommentBy(employeeMinorDTO);
        return commentResponse;
    }
}
