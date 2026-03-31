package com.logistics.supply.model;

import com.logistics.supply.common.annotations.IntegrationTest;
import com.logistics.supply.repository.LocalPurchaseOrderRepository;
import com.logistics.supply.repository.PaymentRepository;
import com.logistics.supply.repository.PettyCashRepository;
import com.logistics.supply.repository.QuotationRepository;
import com.logistics.supply.repository.RequestItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that @SQLRestriction("deleted=false") on each entity filters out
 * soft-deleted rows. For each entity a seeded row (deleted=false) is confirmed
 * visible, then marked deleted via JDBC (bypassing the JPA filter), and
 * confirmed invisible through the repository.
 *
 * @ClearDbBeforeTestMethod (part of @IntegrationTest) re-seeds the DB before
 * every test, so each case starts with a clean deleted=false baseline.
 */
@IntegrationTest
class SoftDeleteFilterTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RequestItemRepository requestItemRepository;

    @Autowired
    private QuotationRepository quotationRepository;

    @Autowired
    private PettyCashRepository pettyCashRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private LocalPurchaseOrderRepository localPurchaseOrderRepository;

    @Test
    void requestItem_softDeleted_isExcludedFromResults() {
        assertThat(requestItemRepository.findById(100)).isPresent();

        jdbcTemplate.update("UPDATE request_item SET deleted = true WHERE id = 100");

        assertThat(requestItemRepository.findById(100)).isEmpty();
    }

    @Test
    void quotation_softDeleted_isExcludedFromResults() {
        assertThat(quotationRepository.findById(100)).isPresent();

        jdbcTemplate.update("UPDATE quotation SET deleted = true WHERE id = 100");

        assertThat(quotationRepository.findById(100)).isEmpty();
    }

    @Test
    void pettyCash_softDeleted_isExcludedFromResults() {
        assertThat(pettyCashRepository.findById(100)).isPresent();

        jdbcTemplate.update("UPDATE petty_cash SET deleted = true WHERE id = 100");

        assertThat(pettyCashRepository.findById(100)).isEmpty();
    }

    @Test
    void payment_softDeleted_isExcludedFromResults() {
        assertThat(paymentRepository.findById(100)).isPresent();

        jdbcTemplate.update("UPDATE payment SET deleted = true WHERE id = 100");

        assertThat(paymentRepository.findById(100)).isEmpty();
    }

    @Test
    void localPurchaseOrder_softDeleted_isExcludedFromResults() {
        assertThat(localPurchaseOrderRepository.findById(100)).isPresent();

        jdbcTemplate.update("UPDATE local_purchase_order SET deleted = true WHERE id = 100");

        assertThat(localPurchaseOrderRepository.findById(100)).isEmpty();
    }
}
