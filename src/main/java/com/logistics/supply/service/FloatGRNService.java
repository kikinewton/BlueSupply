package com.logistics.supply.service;

import com.logistics.supply.model.FloatGRN;
import com.logistics.supply.model.Floats;
import com.logistics.supply.repository.FloatGRNRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FloatGRNService {

  private final FloatGRNRepository floatGRNRepository;

  public FloatGRN save(FloatGRN floatGRN) {
    try {
      return floatGRNRepository.save(floatGRN);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public FloatGRN findFloatGRN(Floats floats) {
    return floatGRNRepository.findByFloats(floats).orElse(null);
  }
}
