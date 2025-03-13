package org.twins.core.mappers.rest.face.navbar;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.face.navbar.FaceNB001MenuItemEntity;
import org.twins.core.dto.rest.face.navbar.nb001.FaceNB001MenuItemDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.resource.ResourceService;


@Component
@RequiredArgsConstructor
public class FaceNB001MenuItemRestDTOMapper extends RestSimpleDTOMapper<FaceNB001MenuItemEntity, FaceNB001MenuItemDTOv1> {
    private final ResourceService resourceService;
    private final I18nService i18nService;

    @MapperModePointerBinding(modes = FaceNB001Modes.FaceNB001MenuItem2FaceMode.class)
    protected final FaceRestDTOMapper faceRestDTOMapper;

    @Override
    public void map(FaceNB001MenuItemEntity src, FaceNB001MenuItemDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setKey(src.getKey())
                .setLabel(i18nService.translateToLocale(src.getLabelI18nId()))
                .setDescription(i18nService.translateToLocale(src.getDescriptionI18nId()))
                .setDisabled(src.getStatus() == FaceNB001MenuItemEntity.Status.DISABLED) //todo
                .setIconLight(resourceService.getResourceUri(src.getIconLightResource()))
                .setIconDark(resourceService.getResourceUri(src.getIconDarkResource()))
                .setTargetPageFaceId(src.getTargetPageFaceId());
        if (mapperContext.hasModeButNot(FaceNB001Modes.FaceNB001MenuItem2FaceMode.HIDE)) {
            faceRestDTOMapper.postpone(src.getTargetPageFace(), mapperContext.forkOnPoint(FaceNB001Modes.FaceNB001MenuItem2FaceMode.SHORT));
        }
    }


}
