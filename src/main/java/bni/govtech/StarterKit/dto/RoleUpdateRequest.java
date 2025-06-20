package bni.govtech.StarterKit.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleUpdateRequest {

    @NotNull(message = "uuid is required")
    private UUID uuid;

    @NotBlank(message = "Role name is required")
    private String name;

    private String description;
}
