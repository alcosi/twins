package org.twins.face.mappers.rest.widget.wt002;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.resource.ResourceService;
import org.twins.face.domain.twidget.wt002.FaceWT002ButtonTwin;
import org.twins.face.dto.rest.widget.wt002.FaceWT002ButtonDTOv1;
import org.twins.face.service.widget.FaceWT002Service;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FaceWT002ButtonRestDTOMapper extends RestSimpleDTOMapper<FaceWT002ButtonTwin, FaceWT002ButtonDTOv1> {
    private final I18nService i18nService;
    private final ResourceService resourceService;
    private final FaceWT002Service faceWT002Service;
    private final FaceWT002ButtonFieldRestDTOMapper faceWT002ButtonFieldRestDTOMapper;


    @MapperModePointerBinding(modes = FaceWT002Modes.FaceWT002Button2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @Override
    public void map(FaceWT002ButtonTwin src, FaceWT002ButtonDTOv1 dst, MapperContext mapperContext) throws Exception {
        List<TwinClassFieldEntity> fields = faceWT002Service.loadFields(src.getButton().getTwinClassId(), src.getButton());
        dst
                .setId(src.getButton().getId())
                .setKey(src.getButton().getKey())
                .setLabel(i18nService.translateToLocale(src.getButton().getLabelI18nId() != null ? src.getButton().getLabelI18nId() : src.getButton().getTwinClass().getNameI18NId()))
                .setIcon(resourceService.getResourceUri(src.getButton().getIconResource()))
                .setStyleClasses(StringUtils.splitToSet(src.getButton().getStyleClasses(), " "))
                .setTwinClassId(src.getButton().getTwinClassId())
                .setExtendsDepth(src.getButton().getExtendsDepth())
                .setFields(faceWT002ButtonFieldRestDTOMapper.convertCollection(fields))
                .setPointedHeadTwinId(faceWT002Service.getHeadTwin(src.getCurrentTwinId(), src.getButton()).getId());

       if (mapperContext.hasModeButNot(FaceWT002Modes.FaceWT002Button2TwinClassMode.HIDE)) {
           twinClassRestDTOMapper.postpone(src.getButton().getTwinClass(), mapperContext.forkOnPoint(FaceWT002Modes.FaceWT002Button2TwinClassMode.SHORT));
       }
    }
}
