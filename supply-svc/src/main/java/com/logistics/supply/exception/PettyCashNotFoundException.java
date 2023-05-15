package com.logistics.supply.exception;

public class PettyCashNotFoundException extends NotFoundException {

  public PettyCashNotFoundException(int pettyCashId) {
    super("Petty cash with id: %s not found".formatted(pettyCashId));
  }
}
