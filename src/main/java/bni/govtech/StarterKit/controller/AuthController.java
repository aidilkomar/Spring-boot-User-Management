package bni.govtech.StarterKit.controller;

import bni.govtech.StarterKit.dto.ApiResponse;
import bni.govtech.StarterKit.dto.LoginRequest;
import bni.govtech.StarterKit.dto.LoginResponse;
import bni.govtech.StarterKit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/test-protected")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public Mono<String> testProtected() {
        return Mono.just("You are SUPERADMIN");
    }


    @GetMapping("/debug-auth")
    public Mono<Map<String, Object>> debugReactiveAuth(Authentication auth) {
        return Mono.just(Map.of(
                "username", auth.getName(),
                "authorities", auth.getAuthorities()
        ));
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<LoginResponse>>> login(@RequestBody LoginRequest request) {
        return authService.signIn(request)
                .map(success -> ResponseEntity.ok(
                        ApiResponse.<LoginResponse>builder()
                                .status(200)
                                .message("sign-in success")
                                .data(success)
                                .build()
                ));
    }
}
