package com.logistics.supply.dto;

import com.logistics.supply.model.GoodsReceivedNote;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import com.logistics.supply.model.Invoice;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class GrnMinorDTO extends MinorDTO {
  private BigDecimal invoiceAmountPayable;
  private InvoiceMinorDTO invoice;
  private EmployeeMinorDTO createdBy;
  private LocalDateTime createdDate;
  private String grnRef;
  private Date paymentDate;

  public static final GrnMinorDTO toDto(GoodsReceivedNote goodsReceivedNote) {
    GrnMinorDTO grnMinorDTO = new GrnMinorDTO();
    BeanUtils.copyProperties(goodsReceivedNote, grnMinorDTO);
    grnMinorDTO.setId((int) goodsReceivedNote.getId());
    InvoiceMinorDTO invoiceMinorDTO = InvoiceMinorDTO.toDto(goodsReceivedNote.getInvoice());
    grnMinorDTO.setInvoice(invoiceMinorDTO);
    EmployeeMinorDTO employeeMinorDTO = EmployeeMinorDTO.toDto(goodsReceivedNote.getCreatedBy());
    grnMinorDTO.setCreatedBy(employeeMinorDTO);
    return grnMinorDTO;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static final class InvoiceMinorDTO extends MinorDTO {
    private String invoiceNumber;
    private SupplierDTO supplier;
    private String invoiceDocument;

    public static final InvoiceMinorDTO toDto(Invoice invoice) {
      InvoiceMinorDTO invoiceMinorDTO = new InvoiceMinorDTO();
      BeanUtils.copyProperties(invoice, invoiceMinorDTO);
      invoiceMinorDTO.setId(invoice.getId());
      SupplierDTO supplierDTO = new SupplierDTO();
      BeanUtils.copyProperties(invoice.getSupplier(), supplierDTO);
      invoiceMinorDTO.setSupplier(supplierDTO);
      String fileName = invoice.getInvoiceDocument().getFileName();
      invoiceMinorDTO.setInvoiceDocument(fileName);
      return invoiceMinorDTO;
    }
  }
}
