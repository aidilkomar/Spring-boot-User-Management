package bni.govtech.StarterKit.service;

import bni.govtech.StarterKit.dto.UserRoleCreateRequest;
import bni.govtech.StarterKit.dto.UserRoleResponse;
import bni.govtech.StarterKit.entity.Role;
import bni.govtech.StarterKit.entity.User;
import bni.govtech.StarterKit.entity.UserRole;
import bni.govtech.StarterKit.repository.RoleRepository;
import bni.govtech.StarterKit.repository.UserRepository;
import bni.govtech.StarterKit.repository.UserRoleRepository;
import bni.govtech.StarterKit.util.UuidConvertUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public Flux<UserRoleResponse> getUserRoleByUser(UUID userUuid) {
        // Cache the user lookup with a longer duration since user data is relatively stable
        Mono<User> cachedUserMono = userRepository.findByUuidAndDeletedAtIsNull(UuidConvertUtil.uuidToBytes(userUuid))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found")))
                .cache(Duration.ofSeconds(5)); // Increased cache duration

        return cachedUserMono
                .flatMapMany(user -> {
                    // Fetch all user roles at once
                    Mono<List<UserRole>> userRolesMono = userRoleRepository.findAllByUserId(user.getId())
                            .collectList()
                            .cache(Duration.ofMillis(500));

                    return userRolesMono.flatMapMany(userRoles -> {
                        if (userRoles.isEmpty()) {
                            return Flux.empty();
                        }

                        // Pre-fetch all role IDs to enable batch fetching
                        List<Long> roleIds = userRoles.stream()
                                .map(UserRole::getRoleId)
                                .collect(Collectors.toList());

                        // Batch fetch all roles in one query if possible
                        Flux<Role> rolesFlux = roleRepository.findAllById(roleIds)
                                .cache(Duration.ofMillis(500));

                        // Create a map for quick lookup
                        Mono<Map<Long, Role>> rolesMapMono = rolesFlux
                                .collectMap(Role::getId)
                                .cache(Duration.ofMillis(500));

                        return rolesMapMono.flatMapMany(rolesMap ->
                                Flux.fromIterable(userRoles)
                                        .map(userRole -> {
                                            Role role = rolesMap.get(userRole.getRoleId());
                                            if (role == null) {
                                                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "role not found");
                                            }
                                            return UserRoleResponse.builder()
                                                    .roleUuid(UuidConvertUtil.bytesToUUID(role.getUuid()))
                                                    .userUuid(UuidConvertUtil.bytesToUUID(user.getUuid()))
                                                    .roleName(role.getName())
                                                    .build();
                                        })
                        );
                    });
                });
    }

    @Override
    public Mono<Void> createUserRole(UserRoleCreateRequest request) {
        // Parallel fetch with dedicated schedulers
        Mono<User> userMono = userRepository.findByUuidAndDeletedAtIsNull(UuidConvertUtil.uuidToBytes(request.getUserId()))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
                .subscribeOn(Schedulers.boundedElastic());  // Offload to IO thread

        Mono<Role> roleMono = roleRepository.findByUuidAndDeletedAtIsNull(UuidConvertUtil.uuidToBytes(request.getRoleId()))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found")))
                .subscribeOn(Schedulers.boundedElastic());  // Parallel execution

        return Mono.zip(userMono, roleMono)
                .timeout(Duration.ofMillis(500))  // Fail fast
                .flatMap(tuple -> {
                    final User user = tuple.getT1();
                    final Role role = tuple.getT2();

                    // Optimized exists check
                    return userRoleRepository.existsByUserIdAndRoleId(user.getId(), role.getId())
                            .flatMap(exists ->
                                    exists ? Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "Mapping exists")) : userRoleRepository.save(
                                            UserRole.builder()
                                                    .roleId(role.getId())
                                                    .userId(user.getId())
                                                    .build()
                                    ).then()
                            );
                })
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))  // Resilience
                .then();
    }
}
