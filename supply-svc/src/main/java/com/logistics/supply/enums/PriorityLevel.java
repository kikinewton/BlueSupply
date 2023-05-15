package com.logistics.supply.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PriorityLevel {
  NORMAL("NORMAL"),
  URGENT("URGENT");
  String priorityLevel;
}
