package bni.govtech.StarterKit.repository;

import bni.govtech.StarterKit.entity.UserVerification;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface UserVerificationRepository extends ReactiveCrudRepository<UserVerification, Long> {

    Mono<UserVerification> findByOtpAndPurposeAndDeletedAtIsNull(String otp, String purpose);

    Mono<UserVerification> findByUuidAndDeletedAtIsNull(String uuid);

    Mono<UserVerification> findByUuidAndDeletedAtIsNull(byte[] uuid);

    Mono<UserVerification> findByOtpAndDeletedAtIsNull(String otp);

    Mono<UserVerification> findByUserIdAndPurposeAndVerifiedAtIsNullAndExpiredAtAfter(Long userId, String purpose, LocalDateTime now);
}
