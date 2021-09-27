package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logistics.supply.annotation.ValidDescription;
import com.logistics.supply.annotation.ValidName;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Data
@NoArgsConstructor
@Entity
public class Department {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(unique = true)
  @ValidName
  private String name;

  @Column @ValidDescription
  String description;

  @JsonIgnore @CreationTimestamp Date createdDate;

  @JsonIgnore Date updatedDate;

  @PostUpdate
  public void logAfterUpdate() {
    updatedDate = new Date();
  }
}
