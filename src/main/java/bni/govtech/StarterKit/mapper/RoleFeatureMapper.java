package bni.govtech.StarterKit.mapper;

import bni.govtech.StarterKit.dto.RoleFeatureCreateRequest;
import bni.govtech.StarterKit.entity.RoleFeature;
import org.springframework.stereotype.Component;

@Component
public class RoleFeatureMapper {

    public RoleFeature toEntity(RoleFeatureCreateRequest request) {
        if (request == null) {
            return null;
        }

        RoleFeature entity = new RoleFeature();
        entity.setCanRead(request.getCanRead());
        entity.setCanCreate(request.getCanCreate());
        entity.setCanUpdate(request.getCanUpdate());
        entity.setCanDelete(request.getCanDelete());
        entity.setCanExport(request.getCanExport());
        entity.setCanApprove(request.getCanApprove());
        return entity;
    }
}
