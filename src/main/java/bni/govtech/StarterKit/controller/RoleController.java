package bni.govtech.StarterKit.controller;

import bni.govtech.StarterKit.dto.*;
import bni.govtech.StarterKit.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;


    @GetMapping
    @PreAuthorize("hasRole('SUPERADMIN')")
    public Mono<ResponseEntity<ApiResponse<List<RoleResponse>>>> getRoles(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

//        int offset = (page - 1) * size;

        return roleService.searchRoles(search, page, size)
                .collectList()
                .map(roles -> ResponseEntity.ok(
                        ApiResponse.<List<RoleResponse>>builder()
                                .status(200)
                                .message("Users fetched successfully")
                                .data(roles)
                                .build()
                ));
    }

    @PreAuthorize("hasRole('SUPERADMIN')")
    @GetMapping("/{uuid}")
    public Mono<ResponseEntity<ApiResponse<RoleResponse>>> getRole(@PathVariable UUID uuid) {
        return roleService.getRoleByUuid(uuid)
                .map(role -> ResponseEntity.ok(
                        ApiResponse.<RoleResponse>builder()
                                .status(200)
                                .message("role fetched successfully")
                                .data(role)
                                .build()
                ))
                .switchIfEmpty(Mono.just(
                        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                                ApiResponse.<RoleResponse>builder()
                                        .status(404)
                                        .message("role not found")
                                        .build()
                        )
                ));
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<Void>>> createRole(@Valid @RequestBody RoleCreateRequest request) {
        return roleService.createRole(request)
                .thenReturn(ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(ApiResponse.<Void>builder()
                                .status(201)
                                .message("role created successfully")
                                .build()));
    }

    @PutMapping
    public Mono<ResponseEntity<ApiResponse<Void>>> updateRole(@Valid @RequestBody RoleUpdateRequest request) {
        return roleService.updateRole(request)
                .thenReturn(ResponseEntity
                        .status(HttpStatus.OK)
                        .body(ApiResponse.<Void>builder()
                                .status(200)
                                .message("role updated successfully")
                                .build())
                );
    }

    @DeleteMapping("/{uuid}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteRole(@PathVariable UUID uuid) {
        return roleService.softDeleteRole(uuid)
                .thenReturn(ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body(ApiResponse.<Void>builder()
                                .status(204)
                                .message("User deleted successfully")
                                .build()));
    }
}
