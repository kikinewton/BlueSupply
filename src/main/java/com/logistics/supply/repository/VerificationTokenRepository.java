package com.logistics.supply.repository;

import com.logistics.supply.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findFirstByEmailAndTokenOrderByIdDesc(String email, String token);
}
