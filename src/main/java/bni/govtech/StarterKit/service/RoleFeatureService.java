package bni.govtech.StarterKit.service;

import bni.govtech.StarterKit.dto.RoleFeatureCreateRequest;
import bni.govtech.StarterKit.dto.RoleFeatureResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RoleFeatureService {
    Flux<RoleFeatureResponse> getRoleFeatureByRole(UUID roleUuid);
    Mono<Void> createRoleFeature(RoleFeatureCreateRequest request);
}
