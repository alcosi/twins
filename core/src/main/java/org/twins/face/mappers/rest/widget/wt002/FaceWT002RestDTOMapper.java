package org.twins.face.mappers.rest.widget.wt002;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.face.dao.widget.wt002.FaceWT002Entity;
import org.twins.face.dto.rest.widget.wt002.FaceWT002DTOv1;
import org.twins.face.service.widget.FaceWT002Service;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class FaceWT002RestDTOMapper extends RestSimpleDTOMapper<FaceWT002Entity, FaceWT002DTOv1> {
    protected final FaceRestDTOMapper faceRestDTOMapper;
    protected final FaceWT002Service faceWT002Service;

    @MapperModePointerBinding(modes = FaceWT002Modes.FaceWT002Button2TwinClassMode.class)
    protected final FaceWT002ButtonRestDTOMapper faceWT002ButtonRestDTOMapper;

    @Override
    public void map(FaceWT002Entity src, FaceWT002DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceRestDTOMapper.map(src.getFace(), dst, mapperContext);
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) {
            case SHORT -> dst
                    .setKey(src.getKey());
            case DETAILED -> {
                faceWT002Service.loadButtons(src);
                dst
                        .setKey(src.getKey())
                        .setStyleClasses(src.getStyleClasses())
                        .setButtons(faceWT002ButtonRestDTOMapper.convertCollection(src.getButtons(), mapperContext));
            }
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<FaceWT002Entity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        faceWT002Service.loadButtons(srcCollection);
    }
}
