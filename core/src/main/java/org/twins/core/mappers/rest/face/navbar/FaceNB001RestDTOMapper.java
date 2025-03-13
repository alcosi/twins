package org.twins.core.mappers.rest.face.navbar;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.face.navbar.FaceNB001Entity;
import org.twins.core.dto.rest.face.navbar.nb001.FaceNB001DTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.core.service.face.navbar.FaceNB001Service;

import java.util.Collection;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {FaceNB001Modes.FaceNB001MenuItemCollectionMode.class})
public class FaceNB001RestDTOMapper extends RestSimpleDTOMapper<FaceNB001Entity, FaceNB001DTOv1> {
    protected final FaceRestDTOMapper faceRestDTOMapper;
    protected final FaceNB001MenuItemRestDTOMapper faceNB001MenuItemRestDTOMapper;
    protected final FaceNB001Service faceNB001Service;

    @Override
    public void map(FaceNB001Entity src, FaceNB001DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceRestDTOMapper.map(src.getFace(), dst, mapperContext);
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) { // perhaps we need some separate mode
            case DETAILED -> dst.setSkin(src.getSkin());
        }
        if (mapperContext.hasModeButNot(FaceNB001Modes.FaceNB001MenuItemCollectionMode.HIDE)) {
            faceNB001Service.loadMenuItems(src);
            dst.setMenuItems(faceNB001MenuItemRestDTOMapper.convertCollection(src.getMenuItems(), mapperContext));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<FaceNB001Entity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(FaceNB001Modes.FaceNB001MenuItemCollectionMode.HIDE)) {
            faceNB001Service.loadMenuItems(srcCollection);
        }
    }
}
