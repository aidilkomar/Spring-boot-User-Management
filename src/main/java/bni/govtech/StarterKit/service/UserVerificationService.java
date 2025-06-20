package bni.govtech.StarterKit.service;

import bni.govtech.StarterKit.entity.UserVerification;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

public interface UserVerificationService {

    Mono<UserVerification> createOrUpdateOtp(Long userId, String purpose);
}
