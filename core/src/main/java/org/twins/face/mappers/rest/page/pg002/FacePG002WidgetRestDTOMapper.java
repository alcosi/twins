package org.twins.face.mappers.rest.page.pg002;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.face.dao.page.pg002.FacePG002WidgetEntity;
import org.twins.face.dto.rest.page.pg002.FacePG002WidgetDTOv1;
import org.twins.face.service.page.FacePG002WidgetService;

import java.util.Collection;


@Component
@RequiredArgsConstructor
public class FacePG002WidgetRestDTOMapper extends RestSimpleDTOMapper<FacePG002WidgetEntity, FacePG002WidgetDTOv1> {

    @MapperModePointerBinding(modes = FacePG002Modes.FacePG002Widget2FaceMode.class)
    protected final FaceRestDTOMapper faceRestDTOMapper;
    protected final FacePG002WidgetService facePG002WidgetService;

    @Override
    public void map(FacePG002WidgetEntity src, FacePG002WidgetDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setWidgetFaceId(src.getWidgetFaceId())
                .setStyleClasses(StringUtils.splitToSet(src.getStyleClasses(), " "))
                .setActive(src.getActive());

        if (mapperContext.hasModeButNot(FacePG002Modes.FacePG002Widget2FaceMode.HIDE)) {
            facePG002WidgetService.loadWidgetFaces(src);
            faceRestDTOMapper.postpone(src.getWidgetFace(), mapperContext.forkOnPoint(FacePG002Modes.FacePG002Widget2FaceMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<FacePG002WidgetEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(FacePG002Modes.FacePG002Widget2FaceMode.HIDE)) {
            facePG002WidgetService.loadWidgetFaces(srcCollection);
        }
    }
}
