package com.logistics.supply.repository;

import com.logistics.supply.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

  Optional<VerificationToken> findFirstByEmailAndTokenOrderByIdDesc(String email, String token);

  @Query(
      value =
          "SELECT EXISTS(SELECT * FROM verification_token vt WHERE now() > vt.expiry_date AND email =:email and token =:token)",
      nativeQuery = true)
  boolean checkTokenIsValid(@Param("email")String email, @Param("token") String token);
}
