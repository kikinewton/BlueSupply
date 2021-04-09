package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logistics.supply.enums.RequestStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Request {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @OneToOne
  private Department userDepartment;

//  @ElementCollection(fetch = FetchType.EAGER)
//  private List<RequestItem> requestItemId;

  @Enumerated(EnumType.STRING)
  private RequestStatus status = RequestStatus.PENDING;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "employee_id", referencedColumnName = "id")
  private Employee procurementOfficer;

  @CreationTimestamp @JsonIgnore Date createdDate;

  @JsonIgnore Date updatedDate;

  @PostUpdate
  public void logAfterUpdate() {
    updatedDate = new Date();
  }
}
