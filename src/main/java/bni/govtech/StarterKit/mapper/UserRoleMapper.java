package bni.govtech.StarterKit.mapper;

import bni.govtech.StarterKit.dto.UserRoleCreateRequest;
import bni.govtech.StarterKit.entity.UserRole;
import org.springframework.stereotype.Component;

@Component
public class UserRoleMapper {

    public UserRole toEntity(UserRoleCreateRequest request, Long userId, Long roleId) {
//        if (request == null) {
//            return null;
//        }

        UserRole entity = new UserRole();
        entity.setUserId(userId);
        entity.setRoleId(roleId);
        return entity;
    }

//    public UserRoleResponse toResponse()
}
