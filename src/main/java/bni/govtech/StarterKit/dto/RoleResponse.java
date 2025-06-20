package bni.govtech.StarterKit.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponse {

    private UUID uuid;

    private String name;

    private String description;
}