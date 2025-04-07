package org.twins.face.mappers.rest.navbar.nb001;

import lombok.RequiredArgsConstructor;
import org.cambium.common.EasyLoggable;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.permission.PermissionRestDTOMapperV2;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.resource.ResourceService;
import org.twins.face.dao.navbar.nb001.FaceNB001MenuItemEntity;
import org.twins.face.dao.navbar.nb001.FaceNB001MenuItemRepository;
import org.twins.face.dto.rest.navbar.nb001.FaceNB001MenuItemDTOv1;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class FaceNB001MenuItemRestDTOMapper extends RestSimpleDTOMapper<FaceNB001MenuItemEntity, FaceNB001MenuItemDTOv1> {
    private final ResourceService resourceService;
    private final I18nService i18nService;
    private final FaceNB001MenuItemRepository faceNB001MenuItemRepository;

    @MapperModePointerBinding(modes = FaceNB001Modes.FaceNB001MenuItem2FaceMode.class)
    protected final FaceRestDTOMapper faceRestDTOMapper;

    @MapperModePointerBinding(modes = FaceNB001Modes.FaceNB001MenuItem2PermissionMode.class)
    private final PermissionRestDTOMapperV2 permissionRestDTOMapper;

    @Override
    public void map(FaceNB001MenuItemEntity src, FaceNB001MenuItemDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setKey(src.getKey())
                .setLabel(i18nService.translateToLocale(src.getLabelI18nId()))
                .setDescription(i18nService.translateToLocale(src.getDescriptionI18nId()))
                .setDisabled(src.getStatus() == FaceNB001MenuItemEntity.Status.DISABLED) //todo
                .setIcon(resourceService.getResourceUri(src.getIconResource()))
                .setTargetPageFaceId(src.getTargetPageFaceId())
                .setPermissionId(src.getPermissionId())
                .setParentFaceMenuItemId(src.getParentFaceMenuItemId())
                .setChilds(mapChildren(src.getId(), mapperContext));

        if (mapperContext.hasModeButNot(FaceNB001Modes.FaceNB001MenuItem2FaceMode.HIDE)) {
            faceRestDTOMapper.postpone(src.getTargetPageFace(), mapperContext.forkOnPoint(FaceNB001Modes.FaceNB001MenuItem2FaceMode.SHORT));
        }

        if (mapperContext.hasModeButNot(FaceNB001Modes.FaceNB001MenuItem2PermissionMode.HIDE)) {
            permissionRestDTOMapper.postpone(src.getPermission(), mapperContext.forkOnPoint(FaceNB001Modes.FaceNB001MenuItem2PermissionMode.SHORT));
        }
    }

    public List<FaceNB001MenuItemDTOv1> mapChildren(UUID parentMenuItemId, MapperContext mapperContext) {
        return faceNB001MenuItemRepository.findByParentFaceMenuItemId(parentMenuItemId)
                .stream()
                .map(childEntity -> {
                    FaceNB001MenuItemDTOv1 dto = new FaceNB001MenuItemDTOv1();
                    try {
                        this.map(childEntity, dto, mapperContext);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to map child menu item: " + childEntity.easyLog(EasyLoggable.Level.NORMAL), e);
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
