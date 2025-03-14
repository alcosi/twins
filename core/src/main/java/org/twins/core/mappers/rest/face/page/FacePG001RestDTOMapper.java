package org.twins.core.mappers.rest.face.page;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.face.page.FacePG001Entity;
import org.twins.core.dto.rest.face.page.pg001.FacePG001DTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.core.service.face.page.FacePG001Service;

import java.util.Collection;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {FacePG001Modes.FacePG001WidgetCollectionMode.class})
public class FacePG001RestDTOMapper extends RestSimpleDTOMapper<FacePG001Entity, FacePG001DTOv1> {
    protected final FaceRestDTOMapper faceRestDTOMapper;
    protected final FacePG001WidgetRestDTOMapper facePG001WidgetRestDTOMapper;
    protected final FacePG001Service facePG001Service;

    @Override
    public void map(FacePG001Entity src, FacePG001DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceRestDTOMapper.map(src.getFace(), dst, mapperContext);
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) { // perhaps we need some separate mode
            // for more fields mapping
        }
        if (mapperContext.hasModeButNot(FacePG001Modes.FacePG001WidgetCollectionMode.HIDE)) {
            facePG001Service.loadWidgets(src);
            dst.setWidgets(facePG001WidgetRestDTOMapper.convertCollection(src.getWidgets(), mapperContext));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<FacePG001Entity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(FacePG001Modes.FacePG001WidgetCollectionMode.HIDE)) {
            facePG001Service.loadWidgets(srcCollection);
        }
    }
}
