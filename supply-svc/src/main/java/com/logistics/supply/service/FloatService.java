package com.logistics.supply.service;

import com.logistics.supply.exception.FloatOrderNotFoundException;
import com.logistics.supply.model.Floats;
import com.logistics.supply.repository.FloatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FloatService {

  private final FloatsRepository floatsRepository;

  public long count() {
    return floatsRepository.count() + 1;
  }

  public Floats findByRef(String floatRef) {
      return floatsRepository.findByFloatRef(floatRef).orElseThrow(() -> new FloatOrderNotFoundException(floatRef));
  }

  public Page<Floats> findAll(Pageable pageable) {
    return floatsRepository.findAll(pageable);
  }

  public Floats findById(int floatId) {
    return floatsRepository.findById(floatId).orElseThrow(() -> new FloatOrderNotFoundException(floatId));
  }
}
