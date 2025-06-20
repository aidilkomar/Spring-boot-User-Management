package bni.govtech.StarterKit.dto;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureUpdateRequest {

    @NotNull(message = "uuid is required")
    private UUID uuid;

    private String name;

    private String description;
}
