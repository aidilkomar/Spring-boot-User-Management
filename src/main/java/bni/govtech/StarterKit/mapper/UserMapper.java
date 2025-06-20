package bni.govtech.StarterKit.mapper;

import bni.govtech.StarterKit.dto.UserCreateRequest;
import bni.govtech.StarterKit.dto.UserResponse;
import bni.govtech.StarterKit.dto.UserUpdateRequest;
import bni.govtech.StarterKit.entity.User;
import bni.govtech.StarterKit.util.PasswordEncoderUtil;
import bni.govtech.StarterKit.util.UuidConvertUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class UserMapper {

    /** Mapping dari Entity ke Response (tanpa password) */
    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .uuid(UuidConvertUtil.bytesToUUID(user.getUuid()))
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    /** Mapping dari CreateRequest ke Entity baru */
    public User toEntity(UserCreateRequest req) {
        return User.builder()
                .uuid(UuidConvertUtil.uuidToBytes(UUID.randomUUID()))
                .username(req.getUsername())
                .email(req.getEmail())
                .password(PasswordEncoderUtil.encode(req.getPassword()))
                .createdAt(LocalDateTime.now())
                .build();
    }

    /** Mapping dari UpdateRequest ke Entity, memodifikasi existing */
    public void updateEntity(UserUpdateRequest req, User existing) {
        existing.setUsername(req.getUsername());
        existing.setEmail(req.getEmail());
//        if (req.getPassword() != null && !req.getPassword().isBlank()) {
//            existing.setPassword(PasswordEncoderUtil.encode(req.getPassword()));
//        }
    }
}
