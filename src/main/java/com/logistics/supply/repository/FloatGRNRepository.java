package com.logistics.supply.repository;

import com.logistics.supply.model.FloatGRN;
import com.logistics.supply.model.Floats;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FloatGRNRepository extends CrudRepository<FloatGRN, Long> {

    Optional<FloatGRN> findByFloats(Floats floats);


}
