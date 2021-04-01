package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@ConfigurationProperties(prefix = "file")
@Entity
@Data
public class RequestDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer employeeId;

    private String fileName;

    private String documentType;

    private String documentFormat;

    private String uploadDirectory;

    @JsonIgnore
    @CreationTimestamp
    private Date createdDate;

    @JsonIgnore
    @UpdateTimestamp
    private Date updatedDate;

}
