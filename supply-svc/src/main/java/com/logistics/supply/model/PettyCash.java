package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import com.logistics.supply.dto.EmployeeMinorDto;
import com.logistics.supply.dto.MinorDto;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestStatus;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Slf4j
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@SQLDelete(sql = "UPDATE petty_cash SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class PettyCash implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Positive private BigDecimal amount;

  @Positive private int quantity;

  @Column(length = 20)
  private String purpose;

  @JsonIgnore private Date approvalDate;

  @JsonIgnore private Date endorsementDate;

  @ManyToOne
  @JoinColumn(name = "department_id")
  private Department department;

  @Column(length = 20)
  private String pettyCashRef;

  @Column(length = 20)
  private String staffId;

  private boolean deleted;

  @Column(nullable = false, length = 100)
  @NotBlank
  private String name;

  @Column(length = 20)
  @Enumerated(EnumType.STRING)
  private RequestStatus status = RequestStatus.PENDING;

  @Column(length = 20)
  @Enumerated(EnumType.STRING)
  private RequestApproval approval = RequestApproval.PENDING;

  @Column(length = 20)
  @Enumerated(EnumType.STRING)
  private EndorsementStatus endorsement = EndorsementStatus.PENDING;

  private boolean paid;

  @CreationTimestamp
  private Date createdDate;

  @UpdateTimestamp
  private Date updatedDate;

  @ManyToOne
  @JoinColumn(name = "created_by")
  private Employee createdBy;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "petty_cash_order_id")
  private PettyCashOrder pettyCashOrder;

  @Getter
  @Setter
  @NoArgsConstructor
  public static final class PettyCashMinorDto extends MinorDto {

    private String staffId;
    private RequestStatus status;
    private RequestApproval approval;
    private EndorsementStatus endorsement;
    private EmployeeMinorDto createdBy;
    private String pettyCashRef;


    public static PettyCashMinorDto toDto(PettyCash pettyCash) {
      PettyCashMinorDto pettyCashMinorDTO = new PettyCashMinorDto();
      BeanUtils.copyProperties(pettyCash, pettyCashMinorDTO);
      if(pettyCash.getCreatedBy() != null){
        EmployeeMinorDto employeeMinorDTO = EmployeeMinorDto.toDto(pettyCash.createdBy);
        pettyCashMinorDTO.setCreatedBy(employeeMinorDTO);
      }
      return pettyCashMinorDTO;
    }
  }
}
