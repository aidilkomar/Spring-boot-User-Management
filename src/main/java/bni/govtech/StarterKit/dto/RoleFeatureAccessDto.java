package bni.govtech.StarterKit.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleFeatureAccessDto {
    private boolean read;
    private boolean create;
    private boolean update;
    private boolean delete;
    private boolean export;
    private boolean approve;
}
