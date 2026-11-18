package org.twins.core.mappers.rest.resource;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.resource.ResourceCountDTOv1;
import org.twins.core.enums.sort.ResourceGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.StorageMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.resource.ResourceService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class ResourceCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<ResourceEntity, ResourceGroupField>, ResourceCountDTOv1> {

    @MapperModePointerBinding(modes = UserMode.Resource2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @MapperModePointerBinding(modes = StorageMode.Resource2StorageMode.class)
    private final StorageRestDTOMapper storageRestDTOMapper;

    private final ResourceService resourceService;

    @Override
    public void map(CountResult<ResourceEntity, ResourceGroupField> src, ResourceCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setUploadedByUserId(entity.getUploadedByUserId())
                .setStorageId(entity.getStorageId())
                .setIsPublic(entity.getIsPublic())
                .setCount(src.getCount());
        if (needLoad(mapperContext, UserMode.Resource2UserMode.HIDE, src, ResourceGroupField.uploadedByUserId)) {
            resourceService.loadUploadedByUser(entity);
            userRestDTOMapper.postpone(entity.getUploadedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.Resource2UserMode.SHORT)));
        }
        if (needLoad(mapperContext, StorageMode.Resource2StorageMode.HIDE, src, ResourceGroupField.storageId)) {
            resourceService.loadStorage(entity);
            storageRestDTOMapper.postpone(entity.getStorage(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(StorageMode.Resource2StorageMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<ResourceEntity, ResourceGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        var entities = srcCollection.stream().map(CountResult::getEntity).toList();
        var someCount = srcCollection.iterator().next();
        if (needLoad(mapperContext, UserMode.Resource2UserMode.HIDE, someCount, ResourceGroupField.uploadedByUserId))
            resourceService.loadUploadedByUser(entities);
        if (needLoad(mapperContext, StorageMode.Resource2StorageMode.HIDE, someCount, ResourceGroupField.storageId))
            resourceService.loadStorage(entities);
    }
}
