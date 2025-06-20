package bni.govtech.StarterKit.service;

import bni.govtech.StarterKit.dto.PaginatedResponse;
import bni.govtech.StarterKit.dto.UserCreateRequest;
import bni.govtech.StarterKit.dto.UserResponse;
import bni.govtech.StarterKit.dto.UserUpdateRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserService {
    Mono<PaginatedResponse<UserResponse>> searchUsers(String search, int offsset, int limit);
    Mono<UserResponse> getUserByUuid(UUID uuid);
    Mono<UserResponse> getUserByUsername(String username);
    Mono<Void> createUser(UserCreateRequest userDto);
    Mono<Void> updateUser(UserUpdateRequest userDto);
    Mono<Void> softDeleteUser(UUID uuid);
}
