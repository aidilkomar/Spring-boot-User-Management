package bni.govtech.StarterKit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleFeatureResponse {
    private UUID roleUuid;
    private String role;
    private List<FeatureAccessDto> features;
}
