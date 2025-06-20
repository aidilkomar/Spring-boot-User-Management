package bni.govtech.StarterKit.service;

import bni.govtech.StarterKit.dto.UserRoleCreateRequest;
import bni.govtech.StarterKit.dto.UserRoleResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserRoleService {

    Flux<UserRoleResponse> getUserRoleByUser(UUID userUuid);

    Mono<Void> createUserRole(UserRoleCreateRequest request);
}
