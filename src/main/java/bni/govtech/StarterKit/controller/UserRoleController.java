package bni.govtech.StarterKit.controller;

import bni.govtech.StarterKit.dto.ApiResponse;
import bni.govtech.StarterKit.dto.UserRoleCreateRequest;
import bni.govtech.StarterKit.dto.UserRoleResponse;
import bni.govtech.StarterKit.service.UserRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user-roles")
@RequiredArgsConstructor
public class UserRoleController {

    private final UserRoleService userRoleService;

    @GetMapping("/{uuid}")
    public Mono<ResponseEntity<ApiResponse<List<UserRoleResponse>>>> getUserRoles(@PathVariable UUID uuid) {
        return userRoleService.getUserRoleByUser(uuid)
                .collectList()
                .map(userRoles -> ResponseEntity.ok(
                        ApiResponse.<List<UserRoleResponse>>builder()
                                .status(200)
                                .message("user roles fetched successfully")
                                .data(userRoles)
                                .build()
                ));
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<Void>>> createUserRole(@Valid @RequestBody UserRoleCreateRequest request) {
        return userRoleService.createUserRole(request)
                .thenReturn(ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(ApiResponse.<Void>builder()
                                .status(201)
                                .message("user role created successfully")
                                .build()));
    }
}
