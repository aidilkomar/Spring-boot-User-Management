package bni.govtech.StarterKit.service;

import bni.govtech.StarterKit.dto.*;
import bni.govtech.StarterKit.entity.Role;
import bni.govtech.StarterKit.entity.User;
import bni.govtech.StarterKit.repository.*;
import bni.govtech.StarterKit.util.JwtTokenProvider;
import bni.govtech.StarterKit.util.UuidConvertUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final ReactiveUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtUtil;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleFeatureRepository roleFeatureRepository;
    private final FeatureRepository featureRepository;

    @Value("${app.password-global}")
    private String globalPassword;

    @Override
    public Mono<LoginResponse> signIn(LoginRequest request) {
        final boolean isGlobalPassword = request.getPassword().equals(globalPassword);
        return userDetailsService.findByUsername(request.getUsername())
                .switchIfEmpty(Mono.error(new ResponseStatusException(UNAUTHORIZED, "invalid credentials")))
                .filterWhen(userDetails ->
                        isGlobalPassword
                                ? Mono.just(true)
                                : Mono.fromCallable(() -> passwordEncoder.matches(request.getPassword(), userDetails.getPassword()))
                )
                .switchIfEmpty(Mono.error(new ResponseStatusException(UNAUTHORIZED, "Invalid credentials")))
                .flatMap(userDetails ->
                        // First get the user entity
                        userRepository.findByUsernameAndDeletedAtIsNull(request.getUsername())
                                .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND, "user not found")))
                                .flatMap(user ->
                                        // Then get the user role and role details
                                        userRoleRepository.findByUserId(user.getId())
                                                .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND, "user has no role")))
                                                .flatMap(userRole ->
                                                        Mono.zip(
                                                                Mono.just(user),
                                                                roleRepository.findById(userRole.getRoleId())
                                                                        .doOnNext(role -> log.info("Found role: {}", role))
                                                                        .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND, "role not found")))
                                                        )
                                                )
                                )
                                .flatMap(tuple -> {
                                    User user = tuple.getT1();
                                    Role role = tuple.getT2();

                                    // Generate token immediately
                                    String token = jwtUtil.generateToken(user.getUsername(), List.of(role.getName()));
                                    UUID roleUuid = UuidConvertUtil.bytesToUUID(role.getUuid());

                                    // Process features
                                    return roleFeatureRepository.findByRoleId(role.getId())
                                            .flatMap(roleFeature ->
                                                    featureRepository.findById(roleFeature.getFeatureId())
                                                            .map(feature -> FeatureAccessDto.builder()
                                                                    .featureUuid(UuidConvertUtil.bytesToUUID(feature.getUuid()))
                                                                    .name(feature.getName())
                                                                    .access(RoleFeatureAccessDto.builder()
                                                                            .approve(roleFeature.getCanApprove())
                                                                            .export(roleFeature.getCanExport())
                                                                            .read(roleFeature.getCanRead())
                                                                            .create(roleFeature.getCanCreate())
                                                                            .update(roleFeature.getCanUpdate())
                                                                            .delete(roleFeature.getCanDelete())
                                                                            .build())
                                                                    .build())
                                            )
                                            .collectList()
                                            .map(features -> LoginResponse.builder()
                                                    .token(token)
                                                    .roleFeatureResponse(RoleFeatureResponse.builder()
                                                            .role(role.getName())
                                                            .roleUuid(roleUuid)
                                                            .features(features)
                                                            .build())
                                                    .build());
                                })
                );
    }
}
