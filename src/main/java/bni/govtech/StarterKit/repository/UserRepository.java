package bni.govtech.StarterKit.repository;

import bni.govtech.StarterKit.entity.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    @Query("""
        SELECT * FROM users
        WHERE (CONTAINS(username, :search, 1) > 0 OR CONTAINS(email, :search, 1) > 0)
          AND deleted_at IS NULL
        ORDER BY created_at DESC
        OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
        """)
    Flux<User> searchUsers(String search, int offset, int limit);
    @Query("SELECT COUNT(*) FROM users WHERE deleted_at IS NULL")
    Mono<Long> countAllByDeletedAtIsNull();
    Mono<User> findByUuidAndDeletedAtIsNull(byte[] uuid);
    Mono<User> findByUsernameAndDeletedAtIsNull(String username);
    Mono<User> findByEmailAndDeletedAtIsNull(String email);
    Mono<User> findByUsernameOrEmailAndDeletedAtIsNull(String username, String email);
    @Query("""
        SELECT * FROM users
        WHERE deleted_at IS NULL
        ORDER BY created_at DESC
        OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
    """)
    Flux<User> findAllByDeletedAtIsNull(int offset, int limit);

    Mono<Boolean> existsByUsernameAndDeletedAtIsNull(String username);

    Mono<Boolean> existsByEmailAndDeletedAtIsNull(String email);

    @Query("""
        SELECT COUNT(*) FROM users
        WHERE (CONTAINS(username, :search, 1) > 0 OR CONTAINS(email, :search, 1) > 0)
          AND deleted_at IS NULL
        """)
    Mono<Long> countSearchUsers(String search);

    @Query("""
        SELECT COUNT(1) FROM users
        WHERE deleted_at IS NULL
    """)
    Mono<Long> countByDeletedAtIsNull();
}
