package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import com.logistics.supply.enums.RequestStatus;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
public class CancelledRequestItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @OneToOne
  private RequestItem requestItem;

  @Column(length = 10)
  @Enumerated(EnumType.STRING)
  private RequestStatus status;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "employee_id", referencedColumnName = "id")
  private Employee employee;

  @CreationTimestamp @JsonIgnore Date createdDate;

  @JsonIgnore @UpdateTimestamp
  Date updatedDate;


}
