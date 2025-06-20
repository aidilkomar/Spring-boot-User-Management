package bni.govtech.StarterKit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Data
@Builder
@AllArgsConstructor
public class PaginatedResponse<T> {
    private Flux<T> datas;
    private Mono<Long> totalRecords;
}
