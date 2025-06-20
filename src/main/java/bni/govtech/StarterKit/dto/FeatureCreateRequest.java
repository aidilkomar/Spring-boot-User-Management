package bni.govtech.StarterKit.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureCreateRequest {

    @NotBlank(message = "name is required")
    private String name;
    private String description;
}
