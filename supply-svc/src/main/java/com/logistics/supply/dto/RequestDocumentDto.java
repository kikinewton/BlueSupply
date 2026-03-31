package com.logistics.supply.dto;

import com.logistics.supply.model.RequestDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
        requestDocumentDTO.setId(requestDocument.getId());
        requestDocumentDTO.setFileName(requestDocument.getFileName());
        requestDocumentDTO.setDocumentType(requestDocument.getDocumentType());
        requestDocumentDTO.setDocumentFormat(requestDocument.getDocumentFormat());
        requestDocument
                .getCreatedBy()
                .ifPresent(employee -> requestDocumentDTO.setCreatedBy(EmployeeMinorDto.toDto(employee)));
        requestDocument.getCreatedDate().ifPresent(requestDocumentDTO::setCreatedDate);
        return requestDocumentDTO;
    }
}
