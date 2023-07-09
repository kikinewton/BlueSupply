package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logistics.supply.enums.RequestStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
public class CancelledRequestItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @OneToOne
  private RequestItem requestItem;

  @Column(length = 10)
  @Enumerated(EnumType.STRING)
  private RequestStatus status;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "employee_id", referencedColumnName = "id")
  private Employee employee;

  @CreationTimestamp @JsonIgnore private Date createdDate;

  @JsonIgnore @UpdateTimestamp
  private Date updatedDate;


}
