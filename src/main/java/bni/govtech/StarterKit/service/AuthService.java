package bni.govtech.StarterKit.service;

import bni.govtech.StarterKit.dto.ApiResponse;
import bni.govtech.StarterKit.dto.LoginRequest;
import bni.govtech.StarterKit.dto.LoginResponse;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface AuthService {
    Mono<LoginResponse> signIn(LoginRequest request);
}
