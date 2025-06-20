package bni.govtech.StarterKit.service;

import bni.govtech.StarterKit.dto.PaginatedResponse;
import bni.govtech.StarterKit.dto.UserCreateRequest;
import bni.govtech.StarterKit.dto.UserResponse;
import bni.govtech.StarterKit.dto.UserUpdateRequest;
import bni.govtech.StarterKit.entity.User;
import bni.govtech.StarterKit.entity.VerificationPurpose;
import bni.govtech.StarterKit.mapper.UserMapper;
import bni.govtech.StarterKit.repository.UserRepository;
import bni.govtech.StarterKit.util.EmailUtil;
import bni.govtech.StarterKit.util.UuidConvertUtil;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class UserServiceImpl extends BaseReactiveService<User> implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserVerificationService userVerificationService;

    @Value("${app.isOtp:false}") // default false kalau gak ada
    private boolean isOtp;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, UserVerificationService userVerificationService) {
        super(
                userRepository::findByUuidAndDeletedAtIsNull,
                userRepository::save
        );
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.userVerificationService = userVerificationService;
    }

    @Override
    public Mono<PaginatedResponse<UserResponse>> searchUsers(String search, int offset, int limit) {
        // Validate pagination parameters
        if (offset < 0 || limit <= 0 || limit > 1000) {
            return Mono.error(new IllegalArgumentException("Invalid pagination"));
        }
        offset = (offset - 1) * limit;

        final boolean useSearch = !StringUtils.isBlank(search);
        final int finalOffset = offset;

        // Get data stream
        Flux<UserResponse> usersFlux = (useSearch ?
                userRepository.searchUsers(search, finalOffset, limit) :
                userRepository.findAllByDeletedAtIsNull(finalOffset, limit))
                .parallel()
                .runOn(Schedulers.parallel())
                .map(userMapper::toResponse)
                .sequential()
                .timeout(Duration.ofSeconds(2))
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100)));

        // Get total count
        Mono<Long> totalMono = useSearch ?
                userRepository.countSearchUsers(search) :
                userRepository.countByDeletedAtIsNull();

        return Mono.zip(usersFlux.collectList(), totalMono)
                .map(tuple -> PaginatedResponse.<UserResponse>builder()
                        .datas(Flux.fromIterable(tuple.getT1()))
                        .totalRecords(Mono.just(tuple.getT2()))
                        .build());
    }

    @Override
    public Mono<UserResponse> getUserByUuid(UUID uuid) {
        return findByUuid(uuid).map(userMapper::toResponse);
    }

    @Override
    public Mono<UserResponse> getUserByUsername(String username) {
        return userRepository.findByUsernameAndDeletedAtIsNull(username)
                .map(userMapper::toResponse);
    }

    @Override
    public Mono<Void> createUser(UserCreateRequest request) {
        // Parallel validation checks
        return Mono.zip(
                        userRepository.existsByUsernameAndDeletedAtIsNull(request.getUsername()),
                        userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())
                )
                .flatMap(existsTuple -> {
                    boolean usernameExists = existsTuple.getT1();
                    boolean emailExists = existsTuple.getT2();

                    if (usernameExists || emailExists) {
                        String errorMessage = usernameExists
                                ? "Username already taken"
                                : "Email already taken";
                        return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, errorMessage));
                    }

                    // Process user creation
                    User user = userMapper.toEntity(request);
                    user.setCreatedAt(LocalDateTime.now());

                    return userRepository.save(user)
                            .subscribeOn(Schedulers.boundedElastic()) // Offload save operation
                            .flatMap(savedUser -> {
                                if (!isOtp) {
                                    return Mono.empty();
                                }

                                return userVerificationService.createOrUpdateOtp(
                                                savedUser.getId(),
                                                VerificationPurpose.EMAIL_VERIFICATION.toString()
                                        )
                                        .timeout(Duration.ofSeconds(5)) // OTP processing timeout
                                        .flatMap(verification -> {
                                            return EmailUtil.sendOtpEmail(
                                                            savedUser.getEmail(),
                                                            verification.getOtp(),
                                                            verification.getExpiredAt().getMinute()
                                                    )
                                                    .subscribeOn(Schedulers.boundedElastic()); // Offload email sending
                                        })
                                        .onErrorResume(e -> {
                                            log.error("Failed to send OTP email", e);
                                            return Mono.empty(); // Continue even if email fails
                                        });
                            });
                })
                .timeout(Duration.ofSeconds(10)) // Overall operation timeout
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100))) // Resiliency
                .then();
    }

    @Override
    public Mono<Void> updateUser(UserUpdateRequest request) {
        return userRepository.findByUuidAndDeletedAtIsNull(UuidConvertUtil.uuidToBytes(request.getUuid()))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
                .flatMap(existingUser -> {
                    // Update fields yang diizinkan di sini, misal username, email
                    existingUser.setUsername(request.getUsername());
                    existingUser.setEmail(request.getEmail());
                    existingUser.setUpdatedAt(LocalDateTime.now());

                    return userRepository.save(existingUser)
                            .flatMap(savedUser -> {
                                if (isOtp) {
                                    return userVerificationService.createOrUpdateOtp(savedUser.getId(), VerificationPurpose.EMAIL_VERIFICATION.toString())
                                            .flatMap(verification -> {
                                                return EmailUtil.sendOtpEmail(
                                                        savedUser.getEmail(),
                                                        verification.getOtp(),
                                                        verification.getExpiredAt().getMinute()
                                                );
                                            });
                                } else {
                                    return Mono.empty();
                                }
                            });
                })
                .then();
    }

    @Override
    public Mono<Void> softDeleteUser(UUID uuid) {
        return softDeleteByUuid(uuid);
    }
}
