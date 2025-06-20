package bni.govtech.StarterKit.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table("roles")
@EqualsAndHashCode(callSuper = true)
public class Role extends BaseEntity {

    @Id
    private Long id;
    private byte[] uuid;
    private String name;
    private String description;
}
