package bni.govtech.StarterKit.service;

import bni.govtech.StarterKit.dto.RoleCreateRequest;
import bni.govtech.StarterKit.dto.RoleResponse;
import bni.govtech.StarterKit.dto.RoleUpdateRequest;
import bni.govtech.StarterKit.entity.Role;
import bni.govtech.StarterKit.mapper.RoleMapper;
import bni.govtech.StarterKit.repository.RoleRepository;
import bni.govtech.StarterKit.util.UuidConvertUtil;
import io.micrometer.common.util.StringUtils;
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

@Service
public class RoleServiceImpl extends BaseReactiveService<Role> implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    public RoleServiceImpl(RoleRepository roleRepository, RoleMapper roleMapper) {
        super(
                roleRepository::findByUuidAndDeletedAtIsNull,
                roleRepository::save
        );
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
    }

    @Override
    public Flux<RoleResponse> searchRoles(String search, int offset, int limit) {

        if (offset < 0 || limit <= 0 || limit > 1000) {
            return Flux.error(new IllegalArgumentException("Invalid pagination parameters"));
        }

        offset = (offset - 1) * limit;
        int finalOffset = offset;
        final boolean useSearch = !StringUtils.isBlank(search);

        return Mono.just(useSearch)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(shouldSearch -> {
                    // Parallel execution paths
                    return shouldSearch
                            ? roleRepository.searchRoles(search, finalOffset, limit)
                            : roleRepository.findAllByDeletedAtIsNull(finalOffset, limit);
                })
                .parallel()
                .runOn(Schedulers.parallel())
                .map(roleMapper::toResponse)
                .sequential()
                .timeout(Duration.ofMillis(500))
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100)));
    }

    @Override
    public Mono<RoleResponse> getRoleByUuid(UUID uuid) {
        return findByUuid(uuid).map(roleMapper::toResponse);
    }

    @Override
    public Mono<Void> createRole(RoleCreateRequest request) {
        return roleRepository.findByNameAndDeletedAtIsNull(request.getName())
                .flatMap(existingRole ->
                    Mono.error(new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "name already taken"
                    ))
                )
                .switchIfEmpty(Mono.defer(() -> {
                    Role role = roleMapper.toEntity(request);
                    role.setCreatedAt(LocalDateTime.now());
                    return roleRepository.save(role);
                }))
                .then();
    }

    @Override
    public Mono<Void> updateRole(RoleUpdateRequest request) {
        return roleRepository.findByUuidAndDeletedAtIsNull(UuidConvertUtil.uuidToBytes(request.getUuid()))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "role not found")))
                .flatMap(existingRole -> {
                    if (!existingRole.getName().equals(request.getName())) {
                        var role = roleRepository.findByNameAndDeletedAtIsNull(request.getName());
                        if (role != null) {
                            return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "name already taken"));
                        }
                    }
                    existingRole.setName(request.getName());
                    existingRole.setDescription(request.getDescription());
                    existingRole.setUpdatedAt(LocalDateTime.now());
                    return roleRepository.save(existingRole);
                })
                .then();
    }

    @Override
    public Mono<Void> softDeleteRole(UUID uuid) {
        return softDeleteByUuid(uuid);
    }
}
