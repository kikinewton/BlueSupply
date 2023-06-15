package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.logistics.supply.dto.EmployeeMinorDTO;
import com.logistics.supply.dto.MinorDTO;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import java.time.LocalDateTime;

@Entity
@Slf4j
@ToString
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
  public static class RequestDocumentDTO extends MinorDTO {
    private String fileName;
    private String documentType;
    private String documentFormat;
    private EmployeeMinorDTO createdBy;
    private LocalDateTime createdDate;

    public static RequestDocumentDTO toDto(RequestDocument requestDocument) {
      RequestDocumentDTO requestDocumentDTO = new RequestDocumentDTO();
      BeanUtils.copyProperties(requestDocument, requestDocumentDTO);
      requestDocumentDTO.setId(requestDocument.getId());
      if (requestDocument.getCreatedBy().isPresent()) {
        EmployeeMinorDTO employeeMinorDTO =
            EmployeeMinorDTO.toDto(requestDocument.getCreatedBy().get());
        requestDocumentDTO.setCreatedBy(employeeMinorDTO);
      }
      if (requestDocument.getCreatedDate().isPresent()) {
        requestDocumentDTO.setCreatedDate(requestDocument.getCreatedDate().get());
      }
      return requestDocumentDTO;
    }
  }
}
