package bni.govtech.StarterKit.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID uuid;
    private String username;
    private String email;
}
