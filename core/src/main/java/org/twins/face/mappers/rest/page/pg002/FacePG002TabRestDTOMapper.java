package org.twins.face.mappers.rest.page.pg002;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.face.dao.page.pg002.FacePG002TabEntity;
import org.twins.face.dto.rest.page.pg002.FacePG002TabDTOv1;
import org.twins.face.service.page.FacePG002WidgetService;

import java.util.Collection;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {FacePG002Modes.FacePG002TabWidgetCollectionMode.class})
public class FacePG002TabRestDTOMapper extends RestSimpleDTOMapper<FacePG002TabEntity, FacePG002TabDTOv1> {
    protected final FaceRestDTOMapper faceRestDTOMapper;
    protected final FacePG002WidgetRestDTOMapper facePG002WidgetRestDTOMapper;
    protected final FacePG002WidgetService facePG002WidgetService;

    @Override
    public void map(FacePG002TabEntity src, FacePG002TabDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) { // perhaps we need some separate mode
            case SHORT -> dst
                    .setId(src.getId())
                    .setTitle(I18nCacheHolder.addId(src.getTitleI18nId()));
            case DETAILED -> dst
                    .setId(src.getId())
                    .setStyleClasses(StringUtils.splitToSet(src.getStyleClasses(), " "))
                    .setTitle(I18nCacheHolder.addId(src.getTitleI18nId()))
                    .setOrder(src.getOrder());
        }
        if (mapperContext.hasModeButNot(FacePG002Modes.FacePG002TabWidgetCollectionMode.HIDE)) {
            facePG002WidgetService.loadWidgets(src);
            dst.setWidgets(facePG002WidgetRestDTOMapper.convertCollection(facePG002WidgetService.filterVariants(src.getWidgets()), mapperContext));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<FacePG002TabEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(FacePG002Modes.FacePG002TabWidgetCollectionMode.HIDE)) {
            facePG002WidgetService.loadWidgets(srcCollection);
        }
    }
}
