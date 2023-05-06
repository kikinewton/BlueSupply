package java.com.logistics.supply.interfaces.projections;

import java.com.logistics.supply.enums.PaymentStatus;
import java.math.BigDecimal;
import java.util.Date;

public interface PaymentMade {
    String getPurchaseNumber();

    BigDecimal getPaymentAmount();

    PaymentStatus getPaymentStatus();

    String getChequeNumber();

    String getBank();

    String getSupplier();

    Date getCreatedDate();
}

