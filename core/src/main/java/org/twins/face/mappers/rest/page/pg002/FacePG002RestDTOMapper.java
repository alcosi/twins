package org.twins.face.mappers.rest.page.pg002;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.face.dao.page.pg002.FacePG002Entity;
import org.twins.face.dao.page.pg002.FacePG002TabEntity;
import org.twins.face.dto.rest.page.pg002.FacePG002DTOv1;
import org.twins.face.service.page.FacePG002TabService;

import java.util.Collection;
import java.util.List;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {FacePG002Modes.FacePG002TabCollectionMode.class})
public class FacePG002RestDTOMapper extends RestSimpleDTOMapper<FacePG002Entity, FacePG002DTOv1> {
    protected final FaceRestDTOMapper faceRestDTOMapper;
    protected final FacePG002TabRestDTOMapper facePG002TabRestDTOMapper;
    protected final FacePG002TabService facePG002TabService;

    @Override
    public void map(FacePG002Entity src, FacePG002DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceRestDTOMapper.map(src.getFace(), dst, mapperContext);
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) { // perhaps we need some separate mode
            case SHORT -> dst
                    .setTitle(I18nCacheHolder.addId(src.getTitleI18nId()));
            case DETAILED -> dst
                    .setLayout(src.getLayout())
                    .setTitle(I18nCacheHolder.addId(src.getTitleI18nId()));
        }
        if (mapperContext.hasModeButNot(FacePG002Modes.FacePG002TabCollectionMode.HIDE)) {
            facePG002TabService.loadTabs(src);
            List<FacePG002TabEntity> sortedList = facePG002TabService.filterVariants(src.getTabs(), FacePG002TabEntity::getOrder);
            dst.setTabs(facePG002TabRestDTOMapper.convertCollection(sortedList, mapperContext));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<FacePG002Entity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(FacePG002Modes.FacePG002TabCollectionMode.HIDE)) {
            facePG002TabService.loadTabs(srcCollection);
        }
    }
}
