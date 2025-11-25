package org.twins.face.mappers.rest.page.pg001;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.face.dao.page.pg001.FacePG001Entity;
import org.twins.face.dto.rest.page.pg001.FacePG001DTOv1;
import org.twins.face.service.page.FacePG001WidgetService;

import java.util.Collection;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {FacePG001Modes.FacePG001WidgetCollectionMode.class})
public class FacePG001RestDTOMapper extends RestSimpleDTOMapper<FacePG001Entity, FacePG001DTOv1> {
    protected final FaceRestDTOMapper faceRestDTOMapper;
    protected final FacePG001WidgetRestDTOMapper facePG001WidgetRestDTOMapper;
    protected final FacePG001WidgetService facePG001WidgetService;

    @Override
    public void map(FacePG001Entity src, FacePG001DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceRestDTOMapper.map(src.getFace(), dst, mapperContext);
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) { // perhaps we need some separate mode
            case SHORT -> dst
                    .setTitle(I18nCacheHolder.addId(src.getTitleI18nId()));
            case DETAILED -> dst
                    .setStyleClasses(StringUtils.splitToSet(src.getStyleClasses(), " "))
                    .setTitle(I18nCacheHolder.addId(src.getTitleI18nId()));
        }
        if (mapperContext.hasModeButNot(FacePG001Modes.FacePG001WidgetCollectionMode.HIDE)) {
            facePG001WidgetService.loadWidgets(src);
            dst.setWidgets(facePG001WidgetRestDTOMapper.convertCollection(facePG001WidgetService.filterVariants(src.getWidgets()), mapperContext));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<FacePG001Entity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(FacePG001Modes.FacePG001WidgetCollectionMode.HIDE)) {
            facePG001WidgetService.loadWidgets(srcCollection);
        }
    }
}
