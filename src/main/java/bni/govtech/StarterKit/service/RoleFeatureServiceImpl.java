package bni.govtech.StarterKit.service;

import bni.govtech.StarterKit.dto.FeatureAccessDto;
import bni.govtech.StarterKit.dto.RoleFeatureAccessDto;
import bni.govtech.StarterKit.dto.RoleFeatureCreateRequest;
import bni.govtech.StarterKit.dto.RoleFeatureResponse;
import bni.govtech.StarterKit.entity.Feature;
import bni.govtech.StarterKit.entity.Role;
import bni.govtech.StarterKit.entity.RoleFeature;
import bni.govtech.StarterKit.mapper.FeatureMapper;
import bni.govtech.StarterKit.mapper.RoleFeatureMapper;
import bni.govtech.StarterKit.repository.FeatureRepository;
import bni.govtech.StarterKit.repository.RoleFeatureRepository;
import bni.govtech.StarterKit.repository.RoleRepository;
import bni.govtech.StarterKit.util.UuidConvertUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleFeatureServiceImpl implements RoleFeatureService {

    private final RoleFeatureRepository roleFeatureRepository;
    private final RoleRepository roleRepository;
    private final FeatureRepository featureRepository;
    private final RoleFeatureMapper roleFeatureMapper;

    @Override
    public Flux<RoleFeatureResponse> getRoleFeatureByRole(UUID roleUuid) {
        // Cache role data to avoid multiple conversions
        Mono<Role> cachedRoleMono = roleRepository.findByUuidAndDeletedAtIsNull(UuidConvertUtil.uuidToBytes(roleUuid))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found")))
                .cache(Duration.ofMillis(500)); // Cache for subsequent subscribers

        return cachedRoleMono
                .flatMapMany(role -> {
                    // Pre-compute role properties
                    final UUID responseRoleUuid = UuidConvertUtil.bytesToUUID(role.getUuid());
                    final String roleName = role.getName();

                    // Batch fetch all role features first
                    return roleFeatureRepository.findByRoleId(role.getId())
                            .collectList() // Small batch optimization
                            .flatMapMany(roleFeatures -> {
                                // Process features with controlled concurrency
                                return Flux.fromIterable(roleFeatures)
                                        .parallel() // Enable parallel processing
                                        .runOn(Schedulers.boundedElastic()) // Use IO-bound scheduler
                                        .flatMap(roleFeature -> featureRepository.findById(roleFeature.getFeatureId())
                                                .map(feature -> createFeatureAccessDto(roleFeature, feature))
                                                .subscribeOn(Schedulers.parallel()) // Per-query scheduler
                                        )
                                        .sequential() // Return to sequential flux
                                        .collectList()
                                        .map(features -> {
                                            RoleFeatureResponse response = new RoleFeatureResponse();
                                            response.setRoleUuid(responseRoleUuid);
                                            response.setRole(roleName);
                                            response.setFeatures(features);
                                            return response;
                                        });
                            });
                })
                .timeout(Duration.ofSeconds(2)) // Prevent hanging
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100))); // Add resilience
    }

    // Optimized DTO creation
    private FeatureAccessDto createFeatureAccessDto(RoleFeature roleFeature, Feature feature) {
        return FeatureAccessDto.builder()
                .featureUuid(UuidConvertUtil.bytesToUUID(feature.getUuid()))
                .name(feature.getName())
                .access(RoleFeatureAccessDto.builder()
                        .read(roleFeature.getCanRead())
                        .create(roleFeature.getCanCreate())
                        .update(roleFeature.getCanUpdate())
                        .delete(roleFeature.getCanDelete())
                        .export(roleFeature.getCanExport())
                        .approve(roleFeature.getCanApprove())
                        .build())
                .build();
    }

    @Override
    public Mono<Void> createRoleFeature(RoleFeatureCreateRequest request) {
        // Parallel fetch with dedicated schedulers
        Mono<Role> roleMono = roleRepository.findByUuidAndDeletedAtIsNull(UuidConvertUtil.uuidToBytes(request.getRoleUuid()))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found")))
                .subscribeOn(Schedulers.boundedElastic());

        Mono<Feature> featureMono = featureRepository.findByUuidAndDeletedAtIsNull(UuidConvertUtil.uuidToBytes(request.getFeatureUuid()))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Feature not found")))
                .subscribeOn(Schedulers.boundedElastic());

        return Mono.zip(roleMono, featureMono)
                .timeout(Duration.ofMillis(500))  // Fail fast
                .flatMap(tuple -> {
                    Role role = tuple.getT1();
                    Feature feature = tuple.getT2();

                    // Optimized exists check
                    return roleFeatureRepository.existsByRoleIdAndFeatureId(role.getId(), feature.getId())
                            .flatMap(exists -> exists
                                    ? Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "Role-Feature mapping already exists"))
                                    : roleFeatureRepository.save(
                                    RoleFeature.builder()
                                            .roleId(role.getId())
                                            .featureId(feature.getId())
                                            .canRead(request.getCanRead())
                                            .canCreate(request.getCanCreate())
                                            .canUpdate(request.getCanUpdate())
                                            .canDelete(request.getCanDelete())
                                            .canExport(request.getCanExport())
                                            .canApprove(request.getCanApprove())
                                            .build()
                                    )
                            )
                            .then();
                })
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))  // Resiliency
                .then();
    }
}
