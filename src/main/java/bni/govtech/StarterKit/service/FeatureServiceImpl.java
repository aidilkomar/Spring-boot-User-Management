package bni.govtech.StarterKit.service;

import bni.govtech.StarterKit.dto.*;
import bni.govtech.StarterKit.entity.Feature;
import bni.govtech.StarterKit.mapper.FeatureMapper;
import bni.govtech.StarterKit.repository.FeatureRepository;
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
public class FeatureServiceImpl extends BaseReactiveService<Feature> implements FeatureService {

    private final FeatureRepository featureRepository;
    private final FeatureMapper featureMapper;

    public FeatureServiceImpl(FeatureRepository featureRepository, FeatureMapper featureMapper) {
        super(
                featureRepository::findByUuidAndDeletedAtIsNull,
                featureRepository::save
        );
        this.featureRepository=featureRepository;
        this.featureMapper=featureMapper;
    }

//    @Override
//    public Flux<FeatureResponse> searchFeatures(String search, int offset, int limit) {
//        if (search == null || search.isBlank()) {
//            // Ambil semua data aktif dengan pagination biasa
//            return featureRepository.findAllByDeletedAtIsNull(offset, limit)
//                    .map(featureMapper::toResponse);
//        } else {
//            // Pakai Oracle Text search
//            return featureRepository.searchFeatures(search, offset, limit)
//                    .map(featureMapper::toResponse);
//        }
//    }

    @Override
    public Mono<PaginatedResponse<FeatureResponse>> searchFeatures(String search, int offset, int limit) {
        // Validate pagination parameters
        if (offset < 0 || limit <= 0 || limit > 1000) {
            return Mono.error(new IllegalArgumentException("Invalid pagination parameters"));
        }

        offset = (offset - 1) * limit;

        // Cache configuration
        final boolean useSearch = !StringUtils.isBlank(search);
        final String cacheKey = useSearch ? "search:" + search : "all";

        int finalOffset = offset;

        Flux<FeatureResponse> featuresFlux = (useSearch
                ? featureRepository.searchFeatures(search, finalOffset, limit)
                : featureRepository.findAllByDeletedAtIsNull(finalOffset, limit))
                .parallel()
                .runOn(Schedulers.parallel())
                .map(featureMapper::toResponse)
                .sequential()
                .timeout(Duration.ofSeconds(2))
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100)));

        Mono<Long> totalMono = useSearch
                ? featureRepository.countSearchFeatures(search)
                : featureRepository.countByDeletedAtIsNull();

        return Mono.zip(featuresFlux.collectList(), totalMono)
                .map(tuple -> PaginatedResponse.<FeatureResponse>builder()
                        .datas(Flux.fromIterable(tuple.getT1()))
                        .totalRecords(Mono.just(tuple.getT2()))
                        .build());
    }

    @Override
    public Mono<FeatureResponse> getFeatureByUuid(UUID uuid) {
        return findByUuid(uuid).map(featureMapper::toResponse);
    }

    @Override
    public Mono<Void> createFeaeture(FeatureCreateRequest request) {
        return featureRepository.findByNameAndDeletedAtIsNull(request.getName())
                .flatMap(existingFeature ->
                    Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "name already taken"))
                )
                .switchIfEmpty(Mono.defer(() -> {
                    Feature feature = featureMapper.toEntity(request);
                    return featureRepository.save(feature);
                }))
                .then()
                ;
    }

    @Override
    public Mono<Void> updateFeature(FeatureUpdateRequest request) {
        return findByUuid(request.getUuid())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "feature not found")))
                .flatMap(existingFeature -> {
                    if (!existingFeature.getName().equals(request.getName())) {
                        var featureName = featureRepository.findByNameAndDeletedAtIsNull(request.getName());
                        if (featureName != null) {
                            Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "name already taken"));
                        }
                    }
                    existingFeature.setName(request.getName());
                    existingFeature.setDescription(request.getDescription());
                    existingFeature.setUpdatedAt(LocalDateTime.now());
                    return featureRepository.save(existingFeature);
                })
                .then();
    }

    @Override
    public Mono<Void> softDeleteFeature(UUID uuid) {
        return softDeleteByUuid(uuid);
    }


}
