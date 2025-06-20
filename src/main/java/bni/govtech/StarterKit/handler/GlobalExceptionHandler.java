package bni.govtech.StarterKit.handler;

import bni.govtech.StarterKit.dto.ApiResponse;
import bni.govtech.StarterKit.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Order(-2) // prioritas tinggi agar menangani error lebih dulu
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Internal Server Error";

        if (ex instanceof ResponseStatusException) {
            ResponseStatusException rse = (ResponseStatusException) ex;
            status = (HttpStatus) rse.getStatusCode();
            message = rse.getReason() != null ? rse.getReason() : rse.getMessage();
        } else if (ex instanceof WebExchangeBindException) {
            WebExchangeBindException bindEx = (WebExchangeBindException) ex;
            status = HttpStatus.BAD_REQUEST;
            message = bindEx.getAllErrors().stream()
                    .map(err -> err.getDefaultMessage())
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Validation error");
        } else if (ex instanceof IllegalArgumentException) {
            status = HttpStatus.BAD_REQUEST;
            message = ex.getMessage();
        } else if (ex instanceof BadCredentialsException) {
            status = HttpStatus.UNAUTHORIZED;
            message = "Username atau password salah"; // atau ex.getMessage() jika ingin menampilkan pesan asli
        }


        ApiResponse<?> apiResponse = ApiResponse.builder()
                .status(status.value())
                .message(message)
                .build();

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        var bufferFactory = exchange.getResponse().bufferFactory();
        var dataBuffer = bufferFactory.wrap(JsonUtil.toJsonBytes(apiResponse));

        log.error("GlobalExceptionHandler caught exception: ", ex);

        return exchange.getResponse().writeWith(Mono.just(dataBuffer));
    }
}