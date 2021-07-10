package com.logistics.supply.model;

import lombok.Data;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import java.util.Date;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
public class Comment extends AbstractAuditable<Employee, Integer> {

    @Column(length = 1000)
    String description;

}
