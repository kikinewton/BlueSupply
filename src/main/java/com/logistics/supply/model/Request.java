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
public class Request {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "requester")
  private Employee requester;

  @OneToOne
  @JoinColumn(name = "request_id")
  private RequestItem requestItemId;

  @Enumerated(EnumType.STRING)
  private RequestStatus status;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "employee_id", referencedColumnName = "id")
  private Employee procurementOfficer;

  @CreationTimestamp @JsonIgnore Date createdDate;

  @UpdateTimestamp @JsonIgnore Date updateDate;
}
