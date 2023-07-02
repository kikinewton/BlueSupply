package com.logistics.supply.dto;

import com.logistics.supply.model.RequestDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class RequestDocumentDto extends MinorDto {
    private String fileName;
    private String documentType;
    private String documentFormat;
    private EmployeeMinorDto createdBy;
    private LocalDateTime createdDate;

    public static RequestDocumentDto toDto(RequestDocument requestDocument) {
        RequestDocumentDto requestDocumentDTO = new RequestDocumentDto();
        BeanUtils.copyProperties(requestDocument, requestDocumentDTO);
        requestDocumentDTO.setId(requestDocument.getId());

        requestDocument
                .getCreatedBy()
                .ifPresent(employee -> requestDocumentDTO.setCreatedBy(EmployeeMinorDto.toDto(employee)));

        requestDocument.getCreatedDate().ifPresent(requestDocumentDTO::setCreatedDate);

        return requestDocumentDTO;
    }
}
