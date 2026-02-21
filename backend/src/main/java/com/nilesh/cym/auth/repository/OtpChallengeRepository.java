package com.nilesh.cym.auth.repository;

import com.nilesh.cym.auth.entity.OtpChallengeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpChallengeRepository extends JpaRepository<OtpChallengeEntity, Long> {

    Optional<OtpChallengeEntity> findTopByMobileAndConsumedFalseOrderByCreatedAtDesc(String mobile);

    Optional<OtpChallengeEntity> findTopByMobileOrderByCreatedAtDesc(String mobile);
}
