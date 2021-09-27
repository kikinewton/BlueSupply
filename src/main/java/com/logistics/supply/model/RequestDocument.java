package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import sun.text.SupplementaryCharacterData;

import javax.persistence.*;
import java.util.Date;

@Entity
@Slf4j
@Data
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(
    value = {"createdDate", "lastModifiedDate", "createdBy", "lastModifiedBy", "new"})
public class RequestDocument extends AbstractAuditable<Employee, Integer> {

  private String fileName;

  private String documentType;

  private String documentFormat;

}
