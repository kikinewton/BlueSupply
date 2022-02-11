package com.logistics.supply.specification;

import com.logistics.supply.model.FloatAgingAnalysis;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class FloatAgeingAnalysisSpecification implements Specification<FloatAgingAnalysis> {
  List<SearchCriteria> list;

  public FloatAgeingAnalysisSpecification() {
    this.list = new ArrayList<>();
  }

  public void add(SearchCriteria criteria) {
    list.add(criteria);
  }

  @Override
  public Predicate toPredicate(
      Root<FloatAgingAnalysis> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder builder) {
    List<Predicate> predicates = new ArrayList<>();
    for (SearchCriteria criteria : list) {
      if (criteria.getOperation().equals(SearchOperation.GREATER_THAN)) {
        predicates.add(
            builder.greaterThan(root.get(criteria.getKey()), criteria.getValue().toString()));
      } else if (criteria.getOperation().equals(SearchOperation.LESS_THAN)) {
        predicates.add(
            builder.lessThan(root.get(criteria.getKey()), criteria.getValue().toString()));
      } else if (criteria.getOperation().equals(SearchOperation.GREATER_THAN_EQUAL)) {
        predicates.add(
            builder.greaterThanOrEqualTo(
                root.get(criteria.getKey()), criteria.getValue().toString()));
      } else if (criteria.getOperation().equals(SearchOperation.LESS_THAN_EQUAL)) {
        predicates.add(
            builder.lessThanOrEqualTo(root.get(criteria.getKey()), criteria.getValue().toString()));
      } else if (criteria.getOperation().equals(SearchOperation.NOT_EQUAL)) {
        predicates.add(builder.notEqual(root.get(criteria.getKey()), criteria.getValue()));
      } else if (criteria.getOperation().equals(SearchOperation.EQUAL)) {
        predicates.add(builder.equal(root.get(criteria.getKey()), criteria.getValue()));
      } else if (criteria.getOperation().equals(SearchOperation.MATCH)) {
        predicates.add(
            builder.like(
                builder.lower(root.get(criteria.getKey())),
                "%" + criteria.getValue().toString().toLowerCase() + "%"));
      } else if (criteria.getOperation().equals(SearchOperation.MATCH_END)) {
        predicates.add(
            builder.like(
                builder.lower(root.get(criteria.getKey())),
                criteria.getValue().toString().toLowerCase() + "%"));
      } else if (criteria.getOperation().equals(SearchOperation.MATCH_START)) {
        predicates.add(
            builder.like(
                builder.lower(root.get(criteria.getKey())),
                "%" + criteria.getValue().toString().toLowerCase()));
      } else if (criteria.getOperation().equals(SearchOperation.IN)) {
        predicates.add(builder.in(root.get(criteria.getKey())).value(criteria.getValue()));
      } else if (criteria.getOperation().equals(SearchOperation.NOT_IN)) {
        predicates.add(builder.not(root.get(criteria.getKey())).in(criteria.getValue()));
      } else if (criteria.getOperation().equals(SearchOperation.IS_NULL)) {
        predicates.add(builder.isNull(root.get(criteria.getKey())));
      }
    }

    return builder.and(predicates.toArray(new Predicate[0]));
  }
}
