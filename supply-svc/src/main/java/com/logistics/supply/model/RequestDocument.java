package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.logistics.supply.dto.EmployeeMinorDto;
import com.logistics.supply.dto.MinorDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(
    value = {"createdDate", "lastModifiedDate", "createdBy", "lastModifiedBy", "new"})
public class RequestDocument extends AbstractAuditable<Employee, Integer> {

  @Column(length = 120)
  private String fileName;

  @Column(length = 10)
  private String documentType;

  @Column(length = 20)
  private String documentFormat;

  @Getter
  @Setter
  @NoArgsConstructor
  public static class RequestDocumentDto extends MinorDto {
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

}
