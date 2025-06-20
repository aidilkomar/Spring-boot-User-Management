package bni.govtech.StarterKit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;
    private PaginationInfo pagination;

    // Untuk response biasa
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .status(HttpStatus.OK.value())
                .data(data)
                .build();
    }

    // Untuk response dengan pesan
    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder()
                .status(HttpStatus.OK.value())
                .message(message)
                .data(data)
                .build();
    }

    // Untuk pagination response
    public static <T> ApiResponse<PaginatedResponse<T>> paginated(
            List<T> content,
            long totalElements
//            int page,
//            int size
    ) {
        PaginatedResponse<T> paginatedResponse = PaginatedResponse.<T>builder()
                .content(content)
                .totalElements(totalElements)
//                .page(page)
//                .size(size)
//                .totalPages((int) Math.ceil((double) totalElements / size))
                .build();

        return ApiResponse.<PaginatedResponse<T>>builder()
                .status(HttpStatus.OK.value())
                .data(paginatedResponse)
                .build();
    }

    // Untuk error response
    public static <T> ApiResponse<T> error(HttpStatus status, String message) {
        return ApiResponse.<T>builder()
                .status(status.value())
                .message(message)
                .build();
    }

    // Inner class untuk pagination
    @Data
    @Builder
    public static class PaginatedResponse<T> {
        private List<T> content;
        private Long totalElements;
//        private int page;
//        private int size;
//        private int totalPages;
    }

    // Inner class untuk info pagination (alternatif)
    @Data
    @Builder
    public static class PaginationInfo {
        private Long totalElements;
//        private int currentPage;
//        private int pageSize;
//        private int totalPages;
    }
}