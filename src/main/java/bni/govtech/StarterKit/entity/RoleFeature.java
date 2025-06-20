package bni.govtech.StarterKit.entity;

import lombok.*;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("role_features")
public class RoleFeature{

    private Long roleId;
    private Long featureId;
    private Boolean canRead;
    private Boolean canCreate;
    private Boolean canUpdate;
    private Boolean canDelete;
    private Boolean canExport;
    private Boolean canApprove;
}

