package bni.govtech.StarterKit.controller;

import bni.govtech.StarterKit.dto.ApiResponse;
import bni.govtech.StarterKit.dto.RoleFeatureCreateRequest;
import bni.govtech.StarterKit.dto.RoleFeatureResponse;
import bni.govtech.StarterKit.service.RoleFeatureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/role-features")
@RequiredArgsConstructor
public class RoleFeatureController {
    private final RoleFeatureService roleFeatureService;

    @GetMapping("/{uuid}")
    public Mono<ResponseEntity<ApiResponse<List<RoleFeatureResponse>>>> getRoleFeatures(@PathVariable UUID uuid) {
        return roleFeatureService.getRoleFeatureByRole(uuid)
                .collectList()
                .map(roleFeatures -> ResponseEntity.ok(
                        ApiResponse.<List<RoleFeatureResponse>>builder()
                                .status(200)
                                .message("role features fetched successfully")
                                .data(roleFeatures)
                                .build()
                ));
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<Void>>> createRoleFeature(@Valid @RequestBody RoleFeatureCreateRequest request) {
        return roleFeatureService.createRoleFeature(request)
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.<Void>builder()
                                .status(201)
                                .message("role feature created successfully")
                                .build()));
    }
}
