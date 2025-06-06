package org.twins.face.mappers.rest.page.pg002;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.core.service.i18n.I18nService;
import org.twins.face.dao.page.pg002.FacePG002Entity;
import org.twins.face.dao.page.pg002.FacePG002TabEntity;
import org.twins.face.dto.rest.page.pg002.FacePG002DTOv1;
import org.twins.face.service.page.FacePG002Service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {FacePG002Modes.FacePG002TabCollectionMode.class})
public class FacePG002RestDTOMapper extends RestSimpleDTOMapper<FacePG002Entity, FacePG002DTOv1> {
    protected final I18nService i18nService;
    protected final FaceRestDTOMapper faceRestDTOMapper;
    protected final FacePG002TabRestDTOMapper facePG002TabRestDTOMapper;
    protected final FacePG002Service facePG002Service;

    @Override
    public void map(FacePG002Entity src, FacePG002DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceRestDTOMapper.map(src.getFace(), dst, mapperContext);
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) { // perhaps we need some separate mode
            case SHORT -> dst
                    .setTitle(i18nService.translateToLocale(src.getTitleI18nId()));
            case DETAILED -> dst
                    .setLayout(src.getLayout())
                    .setTitle(i18nService.translateToLocale(src.getTitleI18nId()));
        }
        if (mapperContext.hasModeButNot(FacePG002Modes.FacePG002TabCollectionMode.HIDE)) {
            facePG002Service.loadTabs(src);
            List<FacePG002TabEntity> sortedList = src.getTabs().getCollection().stream()
                    .sorted(Comparator.comparingInt(FacePG002TabEntity::getOrder))
                    .toList();
            dst.setTabs(facePG002TabRestDTOMapper.convertCollection(sortedList, mapperContext));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<FacePG002Entity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(FacePG002Modes.FacePG002TabCollectionMode.HIDE)) {
            facePG002Service.loadTabs(srcCollection);
        }
    }
}
