package bni.govtech.StarterKit.service;

import bni.govtech.StarterKit.entity.UserVerification;
import bni.govtech.StarterKit.repository.UserVerificationRepository;
import bni.govtech.StarterKit.util.OtpUtil;
import bni.govtech.StarterKit.util.UuidConvertUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserVerificationServiceImpl implements UserVerificationService {

    private final UserVerificationRepository userVerificationRepository;
    private static final int VALIDITY_MINUTES = 10;

    public Mono<UserVerification> createOrUpdateOtp(Long userId, String purpose) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime newExpiredAt = now.plusMinutes(VALIDITY_MINUTES); // misal 30 menit

        return userVerificationRepository
                .findByUserIdAndPurposeAndVerifiedAtIsNullAndExpiredAtAfter(userId, purpose, now)
                .flatMap(existingVerification -> {
                    // Update OTP dan expiredAt
                    existingVerification.setOtp(OtpUtil.generateOtp(6));  // misal 6 digit OTP
                    existingVerification.setExpiredAt(newExpiredAt);
                    existingVerification.setUpdatedAt(now);
                    return userVerificationRepository.save(existingVerification);
                })
                .switchIfEmpty(
                        // Jika tidak ada record aktif, buat baru
                        Mono.defer(() -> {
                            UserVerification newVerification = new UserVerification();
                            newVerification.setUuid(UuidConvertUtil.uuidToBytes(UUID.randomUUID()));
                            newVerification.setUserId(userId);
                            newVerification.setPurpose(purpose);
                            newVerification.setOtp(OtpUtil.generateOtp(6));
                            newVerification.setCreatedAt(now);
                            newVerification.setExpiredAt(newExpiredAt);
                            return userVerificationRepository.save(newVerification);
                        })
                );
    }


    public Mono<Void> verifyOtp(String otp, String purpose) {
        return userVerificationRepository.findByOtpAndPurposeAndDeletedAtIsNull(otp, purpose)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "OTP not found or already used")))
                .flatMap(entity -> {
                    if (entity.getVerifiedAt() != null) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP already used"));
                    }
                    if (entity.getExpiredAt().isBefore(LocalDateTime.now())) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP expired"));
                    }
                    entity.setVerifiedAt(LocalDateTime.now());
                    entity.setUpdatedAt(LocalDateTime.now());
                    return userVerificationRepository.save(entity).then();
                });
    }
}
