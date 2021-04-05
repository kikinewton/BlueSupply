package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.persistence.*;
import java.util.Date;

@Entity
@Slf4j
@Data
public class RequestDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer employeeId;

    private String fileName;

    private String documentType;

    private String documentFormat;

//    @Value("${file.upload-dir}")
//    private String uploadDirectory;

    @JsonIgnore
    @CreationTimestamp
    private Date createdDate;

    @JsonIgnore
    private Date updatedDate;

    @PostUpdate
    private void logAfterUpdate() {
        log.info("Updating the document with id: " + id);
        updatedDate = new Date();
    }
}
