package org.twins.face.mappers.rest.page.pg001;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.face.dao.page.pg001.FacePG001WidgetEntity;
import org.twins.face.dto.rest.page.pg001.FacePG001WidgetDTOv1;


@Component
@RequiredArgsConstructor
public class FacePG001WidgetRestDTOMapper extends RestSimpleDTOMapper<FacePG001WidgetEntity, FacePG001WidgetDTOv1> {

    @MapperModePointerBinding(modes = FacePG001Modes.FacePG001Widget2FaceMode.class)
    protected final FaceRestDTOMapper faceRestDTOMapper;

    @Override
    public void map(FacePG001WidgetEntity src, FacePG001WidgetDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setWidgetFaceId(src.getWidgetFaceId())
                .setContainerItemAttributes(src.getContainerItemAttributes())
                .setColumn(src.getColumn())
                .setRow(src.getRow())
                .setActive(src.isActive());
        if (mapperContext.hasModeButNot(FacePG001Modes.FacePG001Widget2FaceMode.HIDE)) {
            faceRestDTOMapper.postpone(src.getWidgetFace(), mapperContext.forkOnPoint(FacePG001Modes.FacePG001Widget2FaceMode.SHORT));
        }
    }


}
