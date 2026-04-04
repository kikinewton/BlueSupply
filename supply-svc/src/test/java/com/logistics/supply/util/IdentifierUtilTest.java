package com.logistics.supply.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IdentifierUtilTest {

    @Test
    void normalDept_producesCorrectSegments() {
        String ref = IdentifierUtil.idHandler("GRN", "STORES", 42);
        String[] parts = ref.split("-");
        assertThat(parts).hasSize(4);
        assertThat(parts[0]).isEqualTo("GRN");
        assertThat(parts[1]).isEqualTo("STO");
        assertThat(parts[2]).isEqualTo("00000042");
        assertThat(parts[3]).hasSize(4); // DDMM always 4 chars
    }

    @Test
    void shortDept_twoChars_rightPaddedWithX() {
        String ref = IdentifierUtil.idHandler("LPO", "IT", 1);
        String dept = ref.split("-")[1];
        assertThat(dept).isEqualTo("ITX");
    }

    @Test
    void shortDept_oneChar_rightPaddedWithXX() {
        String ref = IdentifierUtil.idHandler("RQI", "A", 1);
        String dept = ref.split("-")[1];
        assertThat(dept).isEqualTo("AXX");
    }

    @Test
    void id_alwaysEightDigits() {
        assertThat(IdentifierUtil.idHandler("QUO", "FINANCE", 1).split("-")[2]).isEqualTo("00000001");
        assertThat(IdentifierUtil.idHandler("QUO", "FINANCE", 9999999).split("-")[2]).isEqualTo("09999999");
        assertThat(IdentifierUtil.idHandler("QUO", "FINANCE", 99999999).split("-")[2]).isEqualTo("99999999");
    }

    @Test
    void dm_alwaysFourChars() {
        String ref = IdentifierUtil.idHandler("PTC", "FINANCE", 5);
        String dm = ref.split("-")[3];
        assertThat(dm).hasSize(4);
        // each two-char half is numeric
        assertThat(dm.substring(0, 2)).matches("\\d{2}");
        assertThat(dm.substring(2, 4)).matches("\\d{2}");
    }

    @Test
    void output_isUpperCase() {
        String ref = IdentifierUtil.idHandler("lpo", "finance", 1);
        assertThat(ref).isEqualTo(ref.toUpperCase());
    }
}
