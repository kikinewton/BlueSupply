package com.logistics.supply.specification;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteria {
  private String key;
  private Object value;
  private SearchOperation operation;
}
