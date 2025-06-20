package bni.govtech.StarterKit.service;

import bni.govtech.StarterKit.dto.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FeatureService {

    Mono<PaginatedResponse<FeatureResponse>> searchFeatures(String search, int offset, int limit);
    Mono<FeatureResponse> getFeatureByUuid(UUID uuid);
    Mono<Void> createFeaeture(FeatureCreateRequest request);
    Mono<Void> updateFeature(FeatureUpdateRequest request);
    Mono<Void> softDeleteFeature(UUID uuid);
}
