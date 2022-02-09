package com.logistics.supply.specification;

import com.logistics.supply.model.FloatAgingAnalysis;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class FloatAgeingAnalysisSpecification implements Specification<FloatAgingAnalysis> {
    @Override
    public Specification<FloatAgingAnalysis> and(Specification<FloatAgingAnalysis> other) {
        return Specification.super.and(other);
    }

    @Override
    public Specification<FloatAgingAnalysis> or(Specification<FloatAgingAnalysis> other) {
        return Specification.super.or(other);
    }

    @Override
    public Predicate toPredicate(Root<FloatAgingAnalysis> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        return null;
    }
}
