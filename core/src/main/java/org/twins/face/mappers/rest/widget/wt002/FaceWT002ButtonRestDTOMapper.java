package org.twins.face.mappers.rest.widget.wt002;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.resource.ResourceService;
import org.twins.face.dao.widget.wt002.FaceWT002ButtonEntity;
import org.twins.face.dto.rest.widget.wt002.FaceWT002ButtonDTOv1;

@Component
@RequiredArgsConstructor
public class FaceWT002ButtonRestDTOMapper extends RestSimpleDTOMapper<FaceWT002ButtonEntity, FaceWT002ButtonDTOv1> {
    private final I18nService i18nService;
    private final ResourceService resourceService;

    @MapperModePointerBinding(modes = FaceWT002Modes.FaceWT002Button2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @Override
    public void map(FaceWT002ButtonEntity src, FaceWT002ButtonDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setKey(src.getKey())
                .setLabel(i18nService.translateToLocale(src.getLabelI18nId() != null ? src.getLabelI18nId() : src.getTwinClass().getNameI18NId()))
                .setIcon(resourceService.getResourceUri(src.getIconResource()))
                .setStyleClasses(StringUtils.splitToSet(src.getStyleClasses(), " "))
                .setTwinClassId(src.getTwinClassId())
                .setExtendsDepth(src.getExtendsDepth());

       if (mapperContext.hasModeButNot(FaceWT002Modes.FaceWT002Button2TwinClassMode.HIDE)) {
           twinClassRestDTOMapper.postpone(src.getTwinClass(), mapperContext.forkOnPoint(FaceWT002Modes.FaceWT002Button2TwinClassMode.SHORT));
       }
    }
}
