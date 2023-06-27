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
public class GrnMinorDto extends MinorDto {
  private BigDecimal invoiceAmountPayable;
  private InvoiceMinorDto invoice;
  private EmployeeMinorDto createdBy;
  private LocalDateTime createdDate;
  private String grnRef;
  private Date paymentDate;

  public static final GrnMinorDto toDto(GoodsReceivedNote goodsReceivedNote) {
    GrnMinorDto grnMinorDTO = new GrnMinorDto();
    BeanUtils.copyProperties(goodsReceivedNote, grnMinorDTO);
    grnMinorDTO.setId((int) goodsReceivedNote.getId());
    InvoiceMinorDto invoiceMinorDTO = InvoiceMinorDto.toDto(goodsReceivedNote.getInvoice());
    grnMinorDTO.setInvoice(invoiceMinorDTO);
    EmployeeMinorDto employeeMinorDTO = EmployeeMinorDto.toDto(goodsReceivedNote.getCreatedBy());
    grnMinorDTO.setCreatedBy(employeeMinorDTO);
    return grnMinorDTO;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static final class InvoiceMinorDto extends MinorDto {
    private String invoiceNumber;
    private SupplierDto supplier;
    private String invoiceDocument;

    public static final InvoiceMinorDto toDto(Invoice invoice) {
      InvoiceMinorDto invoiceMinorDTO = new InvoiceMinorDto();
      BeanUtils.copyProperties(invoice, invoiceMinorDTO);
      invoiceMinorDTO.setId(invoice.getId());
      SupplierDto supplierDTO = new SupplierDto();
      BeanUtils.copyProperties(invoice.getSupplier(), supplierDTO);
      invoiceMinorDTO.setSupplier(supplierDTO);
      String fileName = invoice.getInvoiceDocument().getFileName();
      invoiceMinorDTO.setInvoiceDocument(fileName);
      return invoiceMinorDTO;
    }
  }
}
