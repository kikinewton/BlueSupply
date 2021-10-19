package com.logistics.supply.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.Optional;


public interface VerificationTokenRepository extends CrudRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    @Query(value = "select * from verification_token where user_id =:userId",nativeQuery=true)
    VerificationToken getCorrespondingUser(@Param("userId") int userId);

    @Transactional
    @Modifying
    @Query(value = "delete from verification_token where user_id =:userId",nativeQuery=true)
    void deleteByUserId(@Param("userId") int userId);
}
