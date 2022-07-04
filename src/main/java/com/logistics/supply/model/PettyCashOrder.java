package com.logistics.supply.model;

import com.logistics.supply.dto.EmployeeMinorDTO;
import com.logistics.supply.dto.ItemDTO;
import com.logistics.supply.dto.MinorDTO;
import com.logistics.supply.enums.RequestStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
public class PettyCashOrder extends AbstractAuditable<Employee, Integer> {

  @OneToMany(
      mappedBy = "pettyCashOrder",
      fetch = FetchType.EAGER,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  private Set<PettyCash> pettyCash = new HashSet<>();

  @Column(length = 30)
  private String requestedBy;
  @Column(length = 15)
  private String requestedByPhoneNo;
  @Column(length = 20)
  private String pettyCashOrderRef;
  @Column(length = 15)
  private String staffId;

  @Size(max = 4)
  @ManyToMany(cascade = CascadeType.MERGE)
  @JoinTable(joinColumns = @JoinColumn(name = "petty_cash_order_id"), inverseJoinColumns = @JoinColumn(name = "document_id"))
  List<RequestDocument> supportingDocument;


  public void addPettyCash(PettyCash _pettyCash) {
    this.pettyCash.add(_pettyCash);
    _pettyCash.setPettyCashOrder(this);
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static final class PettyCashOrderDTO extends MinorDTO {
    private String staffId;
    private RequestStatus status;
    private EmployeeMinorDTO createdBy;
    private String pettyCashOrderRef;
    private Set<ItemDTO> pettyCash;
    private List<RequestDocument> supportingDocument;


    public static PettyCashOrderDTO toDto(PettyCashOrder pettyCashOrder) {
      PettyCashOrderDTO pettyCashOrderDTO = new PettyCashOrderDTO();
      BeanUtils.copyProperties(pettyCashOrder, pettyCashOrderDTO);
      if(pettyCashOrder.getCreatedBy().isPresent()){
        EmployeeMinorDTO employeeMinorDTO = EmployeeMinorDTO.toDto(pettyCashOrder.getCreatedBy().get());
        pettyCashOrderDTO.setCreatedBy(employeeMinorDTO);
      }
      if(pettyCashOrder.getPettyCash() !=  null && !pettyCashOrder.getPettyCash().isEmpty()) {
        pettyCashOrder.getPettyCash().forEach(f -> {
          ItemDTO itemDTO = new ItemDTO();
          BeanUtils.copyProperties(f, itemDTO);
          itemDTO.setUnitPrice(f.getAmount());
        });
      }
      if (pettyCashOrder.getSupportingDocument() != null && !pettyCashOrder.getSupportingDocument().isEmpty()) {
        pettyCashOrderDTO.setSupportingDocument(pettyCashOrder.getSupportingDocument());
      }
      return pettyCashOrderDTO;
    }

  }

}
