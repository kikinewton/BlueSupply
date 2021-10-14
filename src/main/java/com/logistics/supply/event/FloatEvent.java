package com.logistics.supply.event;

import com.logistics.supply.model.Floats;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Set;

@Getter
public class FloatEvent extends ApplicationEvent {

  private final String isEndorsed;
  private Set<Floats> floats;

  public FloatEvent(Object source, Set<Floats> floats) {
    super(source);
    this.floats = floats;
    this.isEndorsed =
        floats.stream().map(Floats::getEndorsement).findFirst().get().toString();
  }
}
