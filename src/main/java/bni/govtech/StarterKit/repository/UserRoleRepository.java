package bni.govtech.StarterKit.repository;

import bni.govtech.StarterKit.entity.UserRole;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRoleRepository extends ReactiveCrudRepository<UserRole, Void> {

    @Query("""
            SELECT r.name FROM user_roles ur
            JOIN roles r ON ur.role_id = r.id
            WHERE ur.user_id = :userId
            """)
    Flux<UserRole> findRoleNamesByUserId(Long userId);

    Mono<Object> findByUserIdAndRoleId(Long userId, Long roleId);

    Mono<Boolean> existsByUserIdAndRoleId(Long userId, Long roleId);

    Mono<UserRole> findByUserId(Long userId);

    Flux<UserRole> findAllByUserId(Long userId);
}
