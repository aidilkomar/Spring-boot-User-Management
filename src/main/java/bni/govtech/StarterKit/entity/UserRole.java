package bni.govtech.StarterKit.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table("user_roles")
@EqualsAndHashCode(callSuper = true)
public class UserRole extends BaseEntity {

    private Long userId;
    private Long roleId;
}