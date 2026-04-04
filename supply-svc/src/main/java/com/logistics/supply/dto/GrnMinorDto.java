package com.logistics.supply.dto;

import com.logistics.supply.model.GoodsReceivedNote;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

  public static GrnMinorDto toDto(GoodsReceivedNote goodsReceivedNote) {
    GrnMinorDto grnMinorDTO = new GrnMinorDto();
    grnMinorDTO.setId((int) goodsReceivedNote.getId());
    grnMinorDTO.setInvoiceAmountPayable(goodsReceivedNote.getInvoiceAmountPayable());
    grnMinorDTO.setCreatedDate(goodsReceivedNote.getCreatedDate());
    grnMinorDTO.setGrnRef(goodsReceivedNote.getGrnRef());
    grnMinorDTO.setPaymentDate(goodsReceivedNote.getPaymentDate());
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

    public static InvoiceMinorDto toDto(Invoice invoice) {
      InvoiceMinorDto invoiceMinorDTO = new InvoiceMinorDto();
      invoiceMinorDTO.setId(invoice.getId());
      invoiceMinorDTO.setInvoiceNumber(invoice.getInvoiceNumber());
      invoiceMinorDTO.setSupplier(SupplierDto.toDto(invoice.getSupplier()));
      invoiceMinorDTO.setInvoiceDocument(invoice.getInvoiceDocument().getFileName());
      return invoiceMinorDTO;
    }
  }
}
