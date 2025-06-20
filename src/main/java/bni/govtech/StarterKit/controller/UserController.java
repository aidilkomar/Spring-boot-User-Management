package bni.govtech.StarterKit.controller;

import bni.govtech.StarterKit.dto.*;
import bni.govtech.StarterKit.service.UserService;
import jakarta.validation.Valid;
import lombok.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public Mono<ResponseEntity<ApiResponse<ApiResponse.PaginatedResponse<UserResponse>>>> getUsers(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return userService.searchUsers(search, page, size)
                .flatMap(response -> Mono.zip(
                        response.getDatas().collectList(),
                        response.getTotalRecords(),
                        (users, total) -> ResponseEntity.ok(
                                ApiResponse.<ApiResponse.PaginatedResponse<UserResponse>>builder()
                                        .status(HttpStatus.OK.value())
                                        .message("Users fetched successfully")
                                        .data(ApiResponse.PaginatedResponse.<UserResponse>builder()
                                                .content(users)
                                                .totalElements(total)
//                                                .currentPage(page)
//                                                .totalPages((int) Math.ceil((double) total / size))
                                                .build())
                                        .build()
                        )
                ))
                .timeout(Duration.ofSeconds(3)) // Fail-fast
                .retryWhen(Retry.backoff(2, Duration.ofMillis(100)));
    }

    @GetMapping("/{uuid}")
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> getUser(@PathVariable UUID uuid) {
        return userService.getUserByUuid(uuid)
                .map(user -> ResponseEntity.ok(
                        ApiResponse.<UserResponse>builder()
                                .status(200)
                                .message("User fetched successfully")
                                .data(user)
                                .build()
                ))
                .switchIfEmpty(Mono.just(
                        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                                ApiResponse.<UserResponse>builder()
                                        .status(404)
                                        .message("User not found")
                                        .build()
                        )
                ));
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<Void>>> createUser(@Valid @RequestBody UserCreateRequest request) {
        return userService.createUser(request)
                .thenReturn(ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(ApiResponse.<Void>builder()
                                .status(201)
                                .message("User created successfully")
                                .build()));
    }

    @PutMapping
    public Mono<ResponseEntity<ApiResponse<Void>>> updateUser(@Valid @RequestBody UserUpdateRequest request) {
        return userService.updateUser(request)
                .thenReturn(ResponseEntity.ok(
                        ApiResponse.<Void>builder()
                                .status(200)
                                .message("User updated successfully")
                                .build()
                ));
    }

    @DeleteMapping("/{uuid}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteUser(@PathVariable UUID uuid) {
        return userService.softDeleteUser(uuid)
                .thenReturn(ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body(ApiResponse.<Void>builder()
                                .status(204)
                                .message("User deleted successfully")
                                .build()));
    }
}
