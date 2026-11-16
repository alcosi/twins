package org.twins.core.mappers.rest.resource;

import lombok.RequiredArgsConstructor;
import org.cambium.featurer.FeaturerService;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.resource.StorageEntity;
import org.twins.core.dto.rest.resource.StorageDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerParametrizedRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerMode;
import org.twins.core.mappers.rest.mappercontext.modes.StorageMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = StorageMode.class)
public class StorageRestDTOMapper extends RestSimpleDTOMapper<StorageEntity, StorageDTOv1> {

    @MapperModePointerBinding(modes = FeaturerMode.Storage2FeaturerMode.class)
    private final FeaturerParametrizedRestDTOMapper featurerParametrizedRestDTOMapper;
    private final FeaturerService featurerService;

    @Override
    public void map(StorageEntity src, StorageDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(StorageMode.DETAILED)) {
            case SHORT -> dst
                    .setId(src.getId())
                    .setDescription(src.getDescription())
                    .setStorageFeaturerId(src.getStorageFeaturerId());
            case DETAILED -> dst
                    .setId(src.getId())
                    .setDescription(src.getDescription())
                    .setStorageFeaturerId(src.getStorageFeaturerId())
                    // encrypted params (credentials) are never exposed
                    .setStoragerParams(featurerService.filterEncryptedParams(src.getStorageFeaturerId(), src.getStoragerParams()))
                    .setCreatedAt(src.getCreatedAt() != null ? src.getCreatedAt().toLocalDateTime() : null)
                    .setUpdatedAt(src.getUpdatedAt() != null ? src.getUpdatedAt().toLocalDateTime() : null);
        }
        if (mapperContext.hasModeButNot(FeaturerMode.Storage2FeaturerMode.HIDE)) {
            dst.setStorageFeaturerId(src.getStorageFeaturerId());
            featurerParametrizedRestDTOMapper.postpone(src.getStorageFeaturerId(), src.getStoragerParams(),
                    mapperContext.forkOnPoint(FeaturerMode.Storage2FeaturerMode.SHORT));
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(StorageMode.HIDE);
    }

    @Override
    public String getObjectCacheId(StorageEntity src) {
        return src.getId().toString();
    }
}
