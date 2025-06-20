package bni.govtech.StarterKit.mapper;

import bni.govtech.StarterKit.dto.*;
import bni.govtech.StarterKit.entity.Role;
import bni.govtech.StarterKit.util.UuidConvertUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class RoleMapper {

    public Role toEntity(RoleCreateRequest req) {
        return Role.builder()
                .uuid(UuidConvertUtil.uuidToBytes(UUID.randomUUID()))
                .name(req.getName())
                .description(req.getDescription())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void updateEntity(RoleUpdateRequest req, Role role) {
        role.setName(req.getName());
        role.setDescription(req.getDescription());
        role.setUpdatedAt(LocalDateTime.now());
    }

    public RoleResponse toResponse(Role role) {
        return RoleResponse.builder()
                .uuid(UuidConvertUtil.bytesToUUID(role.getUuid()))
                .name(role.getName())
                .description(role.getDescription())
                .build();
    }
}
