package bni.govtech.StarterKit.repository;

import bni.govtech.StarterKit.entity.RoleFeature;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface RoleFeatureRepository extends ReactiveCrudRepository<RoleFeature, Void> {

    @Query("SELECT * FROM role_features WHERE deleted_at IS NULL")
    Flux<RoleFeature> findAllActive();

    @Query("SELECT * FROM role_features WHERE role_id = :roleId AND feature_id = :featureId")
    Mono<RoleFeature> findByRoleIdAndFeatureId(Long roleId, Long featureId);

    Flux<RoleFeature> findByRoleId(Long id);

    Flux<Boolean> existsByRoleIdAndFeatureId(Long id, Long id1);
}
