package org.twins.face.mappers.rest.navbar.nb001;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.permission.PermissionRestDTOMapper;
import org.twins.core.service.resource.ResourceService;
import org.twins.face.dao.navbar.nb001.FaceNB001MenuItemEntity;
import org.twins.face.dto.rest.navbar.nb001.FaceNB001MenuItemDTOv1;
import org.twins.face.service.navbar.FaceNB001MenuItemService;

import java.util.Collection;


@Component
@RequiredArgsConstructor
public class FaceNB001MenuItemRestDTOMapper extends RestSimpleDTOMapper<FaceNB001MenuItemEntity, FaceNB001MenuItemDTOv1> {
    private final ResourceService resourceService;
    private final FaceNB001MenuItemService faceNB001MenuItemService;

    @MapperModePointerBinding(modes = FaceNB001Modes.FaceNB001MenuItem2FaceMode.class)
    protected final FaceRestDTOMapper faceRestDTOMapper;

    @MapperModePointerBinding(modes = FaceNB001Modes.FaceNB001MenuItem2PermissionMode.class)
    private final PermissionRestDTOMapper permissionRestDTOMapper;

    @Override
    public void map(FaceNB001MenuItemEntity src, FaceNB001MenuItemDTOv1 dst, MapperContext mapperContext) throws Exception {
        faceNB001MenuItemService.loadChilds(src);

        dst
                .setId(src.getId())
                .setKey(src.getKey())
                .setLabel(I18nCacheHolder.addId(src.getLabelI18nId()))
                .setDescription(I18nCacheHolder.addId(src.getDescriptionI18nId()))
                .setDisabled(src.getStatus() == FaceNB001MenuItemEntity.Status.DISABLED) //todo
                .setIcon(resourceService.getResourceUri(src.getIconResource()))
                .setTargetPageFaceId(src.getTargetPageFaceId())
                .setTargetTwinId(src.getTargetTwinId())
                .setGuardedByPermissionId(src.getPermissionId())
                .setParentFaceMenuItemId(src.getParentFaceMenuItemId())
                .setChildren(convertCollection(src.getChilds())); //be afraid of endless looping!


        if (mapperContext.hasModeButNot(FaceNB001Modes.FaceNB001MenuItem2FaceMode.HIDE)) {
            faceRestDTOMapper.postpone(src.getTargetPageFace(), mapperContext.forkOnPoint(FaceNB001Modes.FaceNB001MenuItem2FaceMode.SHORT));
        }

        if (mapperContext.hasModeButNot(FaceNB001Modes.FaceNB001MenuItem2PermissionMode.HIDE)) {
            permissionRestDTOMapper.postpone(src.getPermission(), mapperContext.forkOnPoint(FaceNB001Modes.FaceNB001MenuItem2PermissionMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<FaceNB001MenuItemEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        faceNB001MenuItemService.loadChilds(srcCollection);
    }
}
