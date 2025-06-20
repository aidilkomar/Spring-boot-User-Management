package bni.govtech.StarterKit.repository;

import bni.govtech.StarterKit.entity.Role;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface RoleRepository extends ReactiveCrudRepository<Role, Long> {
    @Query("""
        SELECT * FROM roles
        WHERE (CONTAINS(name, :search, 1) > 0
          AND deleted_at IS NULL
        ORDER BY created_at DESC
        OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
        """)
    Flux<Role> searchRoles(String search, int offset, int limit);

    @Query("""
        SELECT * FROM roles
        WHERE deleted_at IS NULL
        ORDER BY created_at DESC
        OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
    """)
    Flux<Role> findAllByDeletedAtIsNull(int offset, int limit);

    Mono<Role> findByUuidAndDeletedAtIsNull(byte[] bytes);

    Mono<Role> findByNameAndDeletedAtIsNull(String name);
}
