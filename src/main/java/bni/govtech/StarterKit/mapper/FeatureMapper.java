package bni.govtech.StarterKit.mapper;

import bni.govtech.StarterKit.dto.FeatureCreateRequest;
import bni.govtech.StarterKit.dto.FeatureResponse;
import bni.govtech.StarterKit.dto.FeatureUpdateRequest;
import bni.govtech.StarterKit.entity.Feature;
import bni.govtech.StarterKit.util.UuidConvertUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class FeatureMapper {

    public FeatureResponse toResponse(Feature feature) {
        return FeatureResponse.builder()
                .uuid(UuidConvertUtil.bytesToUUID(feature.getUuid()))
                .name(feature.getName())
                .description(feature.getDescription())
                .build();
    }

    public Feature toEntity(FeatureCreateRequest req) {
        return Feature.builder()
                .uuid(UuidConvertUtil.uuidToBytes(UUID.randomUUID()))
                .name(req.getName())
                .description(req.getDescription())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void updateEntity(FeatureUpdateRequest req, Feature existing) {
        existing.setName(req.getName());
        existing.setDescription(req.getDescription());
    }
}
