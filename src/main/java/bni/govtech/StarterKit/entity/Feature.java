package bni.govtech.StarterKit.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table("features")
@EqualsAndHashCode(callSuper = true)
public class Feature extends BaseEntity {

    @Id
    private Long id;
    private byte[] uuid;
    private String name;
    private String description;
}

