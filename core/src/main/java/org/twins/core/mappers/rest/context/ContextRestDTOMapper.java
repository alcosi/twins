package org.twins.core.mappers.rest.context;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.context.ContextEntity;
import org.twins.core.dto.rest.context.ContextDTOv1;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.ContextMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = ContextMode.class)
public class ContextRestDTOMapper extends RestSimpleDTOMapper<ContextEntity, ContextDTOv1> {

    @Override
    public void map(ContextEntity src, ContextDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(ContextMode.DETAILED)) {
            case DETAILED:
                dst
                    .setId(src.getId())
                    .setNameI18nId(src.getNameI18nId())
                    .setDescriptionI18nId(src.getDescriptionI18nId())
                    .setName(I18nCacheHolder.addId(src.getNameI18nId()))
                    .setDescription(I18nCacheHolder.addId(src.getDescriptionI18nId()));
            case SHORT:
                dst
                    .setId(src.getId())
                    .setNameI18nId(src.getNameI18nId())
                    .setDescriptionI18nId(src.getDescriptionI18nId());
        }
    }
}
