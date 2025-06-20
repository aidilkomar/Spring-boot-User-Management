package bni.govtech.StarterKit.service;

import bni.govtech.StarterKit.entity.BaseEntity;
import bni.govtech.StarterKit.util.UuidConvertUtil;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Function;

public abstract class BaseReactiveService<T extends BaseEntity> {

    protected final Function<byte[], Mono<T>> findByUuidFn;
    protected final Function<T, Mono<T>> saveFn;

    protected BaseReactiveService(
            Function<byte[], Mono<T>> findByUuidFn,
            Function<T, Mono<T>> saveFn
    ) {
        this.findByUuidFn = findByUuidFn;
        this.saveFn = saveFn;
    }

    public Mono<T> findByUuid(UUID uuid) {
        return findByUuidFn.apply(UuidConvertUtil.uuidToBytes(uuid))
                .filter(entity -> entity.getDeletedAt() == null);
    }

    public Mono<T> updateByUuid(UUID uuid, Function<T, Mono<T>> updater) {
        return findByUuid(uuid)
                .flatMap(updater)
                .flatMap(entity -> {
                    entity.setUpdatedAt(LocalDateTime.now());
                    return saveFn.apply(entity);
                });
    }

    public Mono<Void> softDeleteByUuid(UUID uuid) {
        return findByUuid(uuid)
                .flatMap(entity -> {
                    entity.setDeletedAt(LocalDateTime.now());
                    return saveFn.apply(entity);
                }).then();
    }
}
