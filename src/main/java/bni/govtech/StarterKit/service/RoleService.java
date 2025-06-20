package bni.govtech.StarterKit.service;

import bni.govtech.StarterKit.dto.RoleCreateRequest;
import bni.govtech.StarterKit.dto.RoleResponse;
import bni.govtech.StarterKit.dto.RoleUpdateRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RoleService {
    Flux<RoleResponse> searchRoles(String search, int offset, int limit);
    Mono<RoleResponse> getRoleByUuid(UUID uuid);
    Mono<Void> createRole(RoleCreateRequest request);
    Mono<Void> updateRole(RoleUpdateRequest request);
    Mono<Void> softDeleteRole(UUID uuid);
}
