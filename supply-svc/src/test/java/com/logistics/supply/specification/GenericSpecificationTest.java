package com.logistics.supply.specification;

import com.logistics.supply.common.annotations.IntegrationTest;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.model.PettyCash;
import com.logistics.supply.repository.PettyCashRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for GenericSpecification<T> covering all SearchOperation values.
 *
 * Tests are divided into two groups:
 *  - REGRESSION: operations that work correctly today and must keep working after refactor.
 *  - BUG: operations that are broken today (they should FAIL against the current code and
 *          PASS after the fixes in GenericSpecification are applied).
 */
@IntegrationTest
class GenericSpecificationTest {

    @Autowired
    private PettyCashRepository pettyCashRepository;

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Saves a minimal valid PettyCash with the given name and amount. */
    private PettyCash savePettyCash(String name, BigDecimal amount) {
        PettyCash p = new PettyCash();
        p.setName(name);
        p.setAmount(amount);
        p.setQuantity(1);
        return pettyCashRepository.save(p);
    }

    private GenericSpecification<PettyCash> spec(String key, Object value, SearchOperation op) {
        GenericSpecification<PettyCash> spec = new GenericSpecification<>();
        spec.add(new SearchCriteria(key, value, op));
        return spec;
    }

    // -----------------------------------------------------------------------
    // REGRESSION TESTS – must pass before AND after the refactor
    // -----------------------------------------------------------------------

    @Test
    void equal_on_string_field_returns_only_matching_item() {
        savePettyCash("Diesel", BigDecimal.TEN);
        savePettyCash("Petrol", BigDecimal.TEN);

        List<PettyCash> result = pettyCashRepository.findAll(
                spec("name", "Diesel", SearchOperation.EQUAL));

        assertThat(result).hasSize(1)
                .extracting(PettyCash::getName)
                .containsExactly("Diesel");
    }

    @Test
    void equal_on_enum_field_returns_only_matching_item() {
        PettyCash approved = savePettyCash("Approved Item", BigDecimal.TEN);
        approved.setApproval(RequestApproval.APPROVED);
        pettyCashRepository.save(approved);

        savePettyCash("Pending Item", BigDecimal.TEN); // default approval is PENDING

        List<PettyCash> result = pettyCashRepository.findAll(
                spec("approval", RequestApproval.APPROVED, SearchOperation.EQUAL));

        assertThat(result).hasSize(1)
                .extracting(PettyCash::getName)
                .containsExactly("Approved Item");
    }

    @Test
    void equal_on_boolean_field_returns_only_matching_items() {
        PettyCash paid = savePettyCash("Paid Item", BigDecimal.TEN);
        paid.setPaid(true);
        pettyCashRepository.save(paid);

        savePettyCash("Unpaid Item", BigDecimal.TEN); // paid defaults to false

        // init_script seeds "Brake fluid" and "Table", both also have paid=false
        List<PettyCash> result = pettyCashRepository.findAll(
                spec("paid", false, SearchOperation.EQUAL));

        assertThat(result).hasSize(3)
                .extracting(PettyCash::getName)
                .containsExactlyInAnyOrder("Brake fluid", "Table", "Unpaid Item");
    }

    @Test
    void not_equal_excludes_matching_item() {
        savePettyCash("Diesel", BigDecimal.TEN);
        savePettyCash("Petrol", BigDecimal.TEN);

        // init_script seeds "Brake fluid" and "Table", both also != "Diesel"
        List<PettyCash> result = pettyCashRepository.findAll(
                spec("name", "Diesel", SearchOperation.NOT_EQUAL));

        assertThat(result).hasSize(3)
                .extracting(PettyCash::getName)
                .containsExactlyInAnyOrder("Brake fluid", "Table", "Petrol");
    }

    @Test
    void match_returns_items_containing_substring() {
        savePettyCash("Engine Oil", BigDecimal.TEN);
        savePettyCash("Oil Filter", BigDecimal.TEN);
        savePettyCash("Brake Pad", BigDecimal.TEN);

        List<PettyCash> result = pettyCashRepository.findAll(
                spec("name", "Oil", SearchOperation.MATCH));

        assertThat(result).hasSize(2)
                .extracting(PettyCash::getName)
                .containsExactlyInAnyOrder("Engine Oil", "Oil Filter");
    }

    @Test
    void match_start_returns_items_starting_with_value() {
        savePettyCash("Diesel Fuel", BigDecimal.TEN);
        savePettyCash("Petrol", BigDecimal.TEN);

        List<PettyCash> result = pettyCashRepository.findAll(
                spec("name", "Diesel", SearchOperation.MATCH_START));

        assertThat(result).hasSize(1)
                .extracting(PettyCash::getName)
                .containsExactly("Diesel Fuel");
    }

    @Test
    void match_end_returns_items_ending_with_value() {
        savePettyCash("Premium Petrol", BigDecimal.TEN);
        savePettyCash("Regular Petrol", BigDecimal.TEN);
        savePettyCash("Diesel", BigDecimal.TEN);

        List<PettyCash> result = pettyCashRepository.findAll(
                spec("name", "Petrol", SearchOperation.MATCH_END));

        assertThat(result).hasSize(2)
                .extracting(PettyCash::getName)
                .containsExactlyInAnyOrder("Premium Petrol", "Regular Petrol");
    }

    @Test
    void in_returns_only_items_whose_field_value_is_in_the_list() {
        savePettyCash("Alpha", BigDecimal.TEN);
        savePettyCash("Beta", BigDecimal.TEN);
        savePettyCash("Gamma", BigDecimal.TEN);

        List<PettyCash> result = pettyCashRepository.findAll(
                spec("name", List.of("Alpha", "Beta"), SearchOperation.IN));

        assertThat(result).hasSize(2)
                .extracting(PettyCash::getName)
                .containsExactlyInAnyOrder("Alpha", "Beta");
    }

    @Test
    void greater_than_equal_and_less_than_equal_work_on_string_fields() {
        savePettyCash("Apple",  BigDecimal.TEN);
        savePettyCash("Mango",  BigDecimal.TEN);
        savePettyCash("Zebra",  BigDecimal.TEN);

        // Seed "Table" (T > M) is included in GTE results; "Brake fluid" (B < M) in LTE results
        List<PettyCash> gte = pettyCashRepository.findAll(
                spec("name", "Mango", SearchOperation.GREATER_THAN_EQUAL));
        assertThat(gte).extracting(PettyCash::getName)
                .containsExactlyInAnyOrder("Mango", "Zebra", "Table");

        List<PettyCash> lte = pettyCashRepository.findAll(
                spec("name", "Mango", SearchOperation.LESS_THAN_EQUAL));
        assertThat(lte).extracting(PettyCash::getName)
                .containsExactlyInAnyOrder("Apple", "Mango", "Brake fluid");
    }

    @Test
    void multiple_criteria_are_combined_with_and_logic() {
        PettyCash a = savePettyCash("Office Supply", BigDecimal.TEN);
        a.setApproval(RequestApproval.APPROVED);
        pettyCashRepository.save(a);

        savePettyCash("Office Supply", BigDecimal.TEN); // same name, PENDING approval
        savePettyCash("Fuel",          BigDecimal.TEN); // different name, PENDING

        GenericSpecification<PettyCash> combined = new GenericSpecification<>();
        combined.add(new SearchCriteria("name",     "Office Supply",        SearchOperation.EQUAL));
        combined.add(new SearchCriteria("approval", RequestApproval.APPROVED, SearchOperation.EQUAL));

        List<PettyCash> result = pettyCashRepository.findAll(combined);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getApproval()).isEqualTo(RequestApproval.APPROVED);
    }

    // -----------------------------------------------------------------------
    // BUG TESTS – these should FAIL against the current code (GenericSpecification
    // has bugs) and PASS after the fixes are applied.
    // -----------------------------------------------------------------------

    /**
     * BUG: NOT_IN is implemented as {@code builder.not(root.get(key)).in(value)}.
     * {@code CriteriaBuilder.not()} expects {@code Expression<Boolean>} but receives
     * a {@code Path<String>}; the resulting SQL is semantically wrong and causes a
     * runtime exception.
     * FIX: {@code builder.not(root.get(key).in(value))}.
     */
    @Test
    void not_in_excludes_items_whose_field_value_is_in_the_list() {
        savePettyCash("Alpha", BigDecimal.TEN);
        savePettyCash("Beta",  BigDecimal.TEN);
        savePettyCash("Gamma", BigDecimal.TEN);

        // Seed "Brake fluid" and "Table" are also not in the exclusion list
        List<PettyCash> result = pettyCashRepository.findAll(
                spec("name", List.of("Alpha", "Beta"), SearchOperation.NOT_IN));

        assertThat(result).hasSize(3)
                .extracting(PettyCash::getName)
                .containsExactlyInAnyOrder("Gamma", "Brake fluid", "Table");
    }

    /**
     * BUG: IS_NULL is not handled by GenericSpecification at all — the branch is
     * simply missing, so no predicate is added and all rows are returned.
     * FIX: add an IS_NULL branch.
     */
    @Test
    void is_null_returns_only_items_where_the_field_is_null() {
        PettyCash withRef = savePettyCash("Has Ref", BigDecimal.TEN);
        withRef.setPettyCashRef("PC-001");
        pettyCashRepository.save(withRef);

        savePettyCash("No Ref", BigDecimal.TEN); // pettyCashRef left null

        List<PettyCash> result = pettyCashRepository.findAll(
                spec("pettyCashRef", null, SearchOperation.IS_NULL));

        assertThat(result).hasSize(1)
                .extracting(PettyCash::getName)
                .containsExactly("No Ref");
    }

    /**
     * BUG: EQUAL_IGNORE_CASE is declared in SearchOperation but has no branch in
     * GenericSpecification, so it silently adds no predicate and returns all rows.
     * FIX: add branch using {@code builder.equal(builder.lower(path), value.toLowerCase())}.
     */
    @Test
    void equal_ignore_case_matches_value_regardless_of_case() {
        savePettyCash("DIESEL", BigDecimal.TEN);
        savePettyCash("Petrol", BigDecimal.TEN);

        List<PettyCash> result = pettyCashRepository.findAll(
                spec("name", "diesel", SearchOperation.EQUAL_IGNORE_CASE));

        assertThat(result).hasSize(1)
                .extracting(PettyCash::getName)
                .containsExactly("DIESEL");
    }

    /**
     * BUG: LIKE is declared in SearchOperation but has no branch in GenericSpecification,
     * so it silently adds no predicate and returns all rows.
     * FIX: add branch using {@code builder.like(root.get(key), pattern)} where the caller
     * provides a raw SQL LIKE pattern (e.g. "%Petrol%").
     */
    @Test
    void like_applies_raw_sql_pattern_to_field() {
        savePettyCash("Stationary Pack", BigDecimal.TEN);
        savePettyCash("Fuel",            BigDecimal.TEN);

        List<PettyCash> result = pettyCashRepository.findAll(
                spec("name", "%Pack%", SearchOperation.LIKE));

        assertThat(result).hasSize(1)
                .extracting(PettyCash::getName)
                .containsExactly("Stationary Pack");
    }
}