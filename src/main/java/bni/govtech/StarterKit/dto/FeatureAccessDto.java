package bni.govtech.StarterKit.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class FeatureAccessDto {
    private UUID featureUuid;
    private String name;
    private RoleFeatureAccessDto access;
}
