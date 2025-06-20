package bni.govtech.StarterKit.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table("users")
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity{

    @Id
    private Long id;
    @Column("uuid")
    private byte[] uuid;
    private String username;
    private String email;
    private String password;
}
