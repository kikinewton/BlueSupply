package com.logistics.supply.model;

import lombok.Getter;
import org.springframework.data.annotation.Immutable;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Getter
@Immutable
@Table(name = "spend_by_category_view")
public class SpendByCategory {

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "total_spend")
    private BigDecimal totalSpend;

    @Column(name = "item_count")
    private long itemCount;
}
