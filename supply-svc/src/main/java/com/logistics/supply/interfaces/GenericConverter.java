package com.logistics.supply.interfaces;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface GenericConverter<I, O> extends Function<I, O> {

  default O convert(@NonNull final I input) {
    O output = null;
    return this.apply(input);
  }

  default List<O> convert(final List<I> input) {
    List<O> output = new ArrayList<>();
    if (input != null) {
      output = input.stream().map(this::apply).collect(Collectors.toList());
    }
    output.removeIf(e -> e == null);
    return output;
  }
}
