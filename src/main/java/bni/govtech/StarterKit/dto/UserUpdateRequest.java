package bni.govtech.StarterKit.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class UserUpdateRequest {
    @NotNull
    private UUID uuid;

    @NotBlank
    private String username;

    @Email @NotBlank
    private String email;

    // Jika password boleh berubah, buat optional
//    @Size(min = 8)
//    private String password;
}
