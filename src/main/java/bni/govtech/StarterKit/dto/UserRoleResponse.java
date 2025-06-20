package bni.govtech.StarterKit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoleResponse {

    private UUID userUuid;

    private UUID roleUuid;

    private String roleName;
}
