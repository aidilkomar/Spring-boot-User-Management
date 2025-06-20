package bni.govtech.StarterKit.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table("user_verification")
@EqualsAndHashCode(callSuper = true)
public class UserVerification extends BaseEntity {

    @Id
    private Long id;

    private byte[] uuid;
    private Long userId;

    private String purpose;
    private String otp;

    private LocalDateTime expiredAt;
    private LocalDateTime verifiedAt;

}
