package bni.govtech.StarterKit.controller;

import bni.govtech.StarterKit.dto.*;
import bni.govtech.StarterKit.service.FeatureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/features")
@RequiredArgsConstructor
public class FeatureController {

    private final FeatureService featureService;

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<ApiResponse.PaginatedResponse<FeatureResponse>>>> getFeatures(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

//        int offset = (page - 1) * size;

        return featureService.searchFeatures(search, page, size)
                .flatMap(response -> Mono.zip(
                        response.getDatas().collectList(),
                        response.getTotalRecords(),
                                (features, total) -> ResponseEntity.ok(
                                        ApiResponse.<ApiResponse.PaginatedResponse<FeatureResponse>>builder()
                                                .status(HttpStatus.OK.value())
                                                .message("feature fetch successfully")
                                                .data(ApiResponse.PaginatedResponse.<FeatureResponse>builder()
                                                        .content(features)
                                                        .totalElements(total)
                                                        .build())
                                                .build()
                                )
                ))
                .timeout(Duration.ofSeconds(3)) // Fail-fast
                .retryWhen(Retry.backoff(2, Duration.ofMillis(100)));
    }

    @GetMapping("/{uuid}")
    public Mono<ResponseEntity<ApiResponse<FeatureResponse>>> getFeature(@PathVariable UUID uuid) {
        return featureService.getFeatureByUuid(uuid)
                .map(role -> ResponseEntity.ok(
                        ApiResponse.<FeatureResponse>builder()
                                .status(200)
                                .message("feature fetched successfully")
                                .data(role)
                                .build()
                ))
                .switchIfEmpty(Mono.just(
                        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                                ApiResponse.<FeatureResponse>builder()
                                        .status(404)
                                        .message("feature not found")
                                        .build()
                        )
                ));
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<Void>>> createFeature(@Valid @RequestBody FeatureCreateRequest request) {
        return featureService.createFeaeture(request)
                .thenReturn(ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(ApiResponse.<Void>builder()
                                .status(201)
                                .message("feature created successfully")
                                .build()));
    }

    @PutMapping
    public Mono<ResponseEntity<ApiResponse<Void>>> updateFeature(@Valid @RequestBody FeatureUpdateRequest request) {
        return featureService.updateFeature(request)
                .thenReturn(ResponseEntity
                        .status(HttpStatus.OK)
                        .body(ApiResponse.<Void>builder()
                                .status(200)
                                .message("feature updated successfully")
                                .build())
                );
    }

    @DeleteMapping("/{uuid}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteFeature(@PathVariable UUID uuid) {
        return featureService.softDeleteFeature(uuid)
                .thenReturn(ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body(ApiResponse.<Void>builder()
                                .status(204)
                                .message("feature deleted successfully")
                                .build()));
    }
}
