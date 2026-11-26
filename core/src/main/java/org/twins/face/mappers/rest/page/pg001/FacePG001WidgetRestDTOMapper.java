package org.twins.face.mappers.rest.page.pg001;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.face.dao.page.pg001.FacePG001WidgetEntity;
import org.twins.face.dto.rest.page.pg001.FacePG001WidgetDTOv1;
import org.twins.face.service.page.FacePG001WidgetService;

import java.util.Collection;


@Component
@RequiredArgsConstructor
public class FacePG001WidgetRestDTOMapper extends RestSimpleDTOMapper<FacePG001WidgetEntity, FacePG001WidgetDTOv1> {

    @MapperModePointerBinding(modes = FacePG001Modes.FacePG001Widget2FaceMode.class)
    protected final FaceRestDTOMapper faceRestDTOMapper;
    protected final FacePG001WidgetService facePG001WidgetService;

    @Override
    public void map(FacePG001WidgetEntity src, FacePG001WidgetDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setWidgetFaceId(src.getWidgetFaceId())
                .setStyleClasses(StringUtils.splitToSet(src.getStyleClasses(), " "))
                .setActive(src.getActive());

        if (mapperContext.hasModeButNot(FacePG001Modes.FacePG001Widget2FaceMode.HIDE)) {
            facePG001WidgetService.loadWidgetFaces(src);
            faceRestDTOMapper.postpone(src.getWidgetFace(), mapperContext.forkOnPoint(FacePG001Modes.FacePG001Widget2FaceMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<FacePG001WidgetEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(FacePG001Modes.FacePG001Widget2FaceMode.HIDE)) {
            facePG001WidgetService.loadWidgetFaces(srcCollection);
        }
    }
}
