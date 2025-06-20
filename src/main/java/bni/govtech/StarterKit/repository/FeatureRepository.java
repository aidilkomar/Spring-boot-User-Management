package bni.govtech.StarterKit.repository;

import bni.govtech.StarterKit.entity.Feature;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface FeatureRepository extends ReactiveCrudRepository<Feature, Long> {
    @Query("""
        SELECT * FROM features
        WHERE (CONTAINS(name, :search, 1) > 0
          AND deleted_at IS NULL
        ORDER BY created_at DESC
        OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
        """)
    Flux<Feature> searchFeatures(String search, int offset, int limit);

    @Query("""
        SELECT * FROM features
        WHERE deleted_at IS NULL
        ORDER BY created_at DESC
        OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
    """)
    Flux<Feature> findAllByDeletedAtIsNull(int offset, int limit);

    Mono<Feature> findByUuidAndDeletedAtIsNull(byte[] bytes);

    Mono<Feature> findByNameAndDeletedAtIsNull(String name);

    @Query("""
            SELECT COUNT(1) FROM features
            WHERE (CONTAINS(name, :search, 1) > 0
              AND deleted_at IS NULL
            """)
    Mono<Long> countSearchFeatures(String search);

    @Query("""
            SELECT COUNT(1) FROM features
            WHERE deleted_at IS NULL
            """)
    Mono<Long> countByDeletedAtIsNull();
}
