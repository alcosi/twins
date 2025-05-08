package org.twins.face.mappers.rest.page.pg002;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.core.service.i18n.I18nService;
import org.twins.face.dao.page.pg002.FacePG002TabEntity;
import org.twins.face.dto.rest.page.pg002.FacePG002TabDTOv1;
import org.twins.face.dto.rest.page.pg002.FacePG002WidgetDTOv1;
import org.twins.face.service.page.FacePG002Service;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {FacePG002Modes.FacePG002TabWidgetCollectionMode.class})
public class FacePG002TabRestDTOMapper extends RestSimpleDTOMapper<FacePG002TabEntity, FacePG002TabDTOv1> {
    protected final I18nService i18nService;
    protected final FaceRestDTOMapper faceRestDTOMapper;
    protected final FacePG002WidgetRestDTOMapper facePG002WidgetRestDTOMapper;
    protected final FacePG002Service facePG002Service;

    @Override
    public void map(FacePG002TabEntity src, FacePG002TabDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) { // perhaps we need some separate mode
            case SHORT -> dst
                    .setId(src.getId())
                    .setTitle(i18nService.translateToLocale(src.getTitleI18nId()));
            case DETAILED -> dst
                    .setId(src.getId())
                    .setStyleClasses(StringUtils.splitToSet(src.getStyleClasses(), " "))
                    .setTitle(i18nService.translateToLocale(src.getTitleI18nId()));
        }
        if (mapperContext.hasModeButNot(FacePG002Modes.FacePG002TabWidgetCollectionMode.HIDE)) {
            facePG002Service.loadWidgets(src);
            dst.setWidgets(facePG002WidgetRestDTOMapper.convertCollection(src.getWidgets(), mapperContext));
            //todo delete me after layout support
            dst.setWidgets(dst.getWidgets().stream()
                    .sorted(Comparator.comparingInt(FacePG002WidgetDTOv1::getRow))
                    .collect(Collectors.toList()));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<FacePG002TabEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(FacePG002Modes.FacePG002TabWidgetCollectionMode.HIDE)) {
            facePG002Service.loadWidgets(srcCollection);
        }
    }
}
