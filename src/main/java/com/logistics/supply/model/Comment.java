//package com.logistics.supply.model;
//
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import lombok.ToString;
//import org.hibernate.annotations.CreationTimestamp;
//import org.springframework.data.jpa.domain.AbstractAuditable;
//import org.springframework.data.jpa.domain.support.AuditingEntityListener;
//
//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.EntityListeners;
//import javax.persistence.MappedSuperclass;
//import java.io.Serializable;
//import java.util.Date;
//
//@Entity
//@Data
//@EntityListeners(AuditingEntityListener.class)
//@ToString
//@NoArgsConstructor
//@MappedSuperclass
//@JsonIgnoreProperties(
//        value = {"createdDate", "lastModifiedDate", "createdBy", "lastModifiedBy", "new"})
//public class Comment extends AbstractAuditable<Employee, Integer> implements Serializable {
//
//    @Column(length = 1000)
//    String description;
//
//    Boolean read;
//
//
//
//
//}
