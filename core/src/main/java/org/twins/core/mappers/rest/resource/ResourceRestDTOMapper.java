package org.twins.core.mappers.rest.resource;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dto.rest.resource.ResourceDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.StorageMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.resource.ResourceService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class ResourceRestDTOMapper extends RestSimpleDTOMapper<ResourceEntity, ResourceDTOv1> {

    @MapperModePointerBinding(modes = UserMode.Resource2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @MapperModePointerBinding(modes = StorageMode.Resource2StorageMode.class)
    private final StorageRestDTOMapper storageRestDTOMapper;

    private final ResourceService resourceService;

    @Override
    public void map(ResourceEntity src, ResourceDTOv1 dst, MapperContext mapperContext) throws Exception {
        resourceService.loadStorage(src);
        dst
                .setId(src.getId())
                .setUploadedByUserId(src.getUploadedByUserId())
                .setStorageId(src.getStorageId())
                .setOriginalFileName(src.getOriginalFileName())
                .setSizeInBytes(src.getSizeInBytes())
                .setCreatedAt(src.getCreatedAt() != null ? src.getCreatedAt().toLocalDateTime() : null)
                .setIsPublic(src.getIsPublic())
                .setStorageLink(resourceService.getResourceUri(src));
        if (mapperContext.hasModeButNot(UserMode.Resource2UserMode.HIDE)) {
            resourceService.loadUploadedByUser(src);
            userRestDTOMapper.postpone(src.getUploadedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.Resource2UserMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(StorageMode.Resource2StorageMode.HIDE)) {
            storageRestDTOMapper.postpone(src.getStorage(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(StorageMode.Resource2StorageMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<ResourceEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        // storage is needed unconditionally: map() builds storageLink for every item
        resourceService.loadStorage(srcCollection);
        if (mapperContext.hasModeButNot(UserMode.Resource2UserMode.HIDE))
            resourceService.loadUploadedByUser(srcCollection);
    }

}
