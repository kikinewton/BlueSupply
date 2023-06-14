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

  @Column(unique = true, length = 50)
  @ValidName
  private String name;

  @Column(length = 50)
  @ValidDescription
  String description;

  @JsonIgnore
  @CreationTimestamp
  private Date createdDate;

  @JsonIgnore
  @UpdateTimestamp
  private Date updatedDate;
}
