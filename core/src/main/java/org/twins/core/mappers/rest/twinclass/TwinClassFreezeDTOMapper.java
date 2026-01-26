package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinclass.TwinClassFreezeEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFreezeDTOv1;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.StatusMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassFreezeMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.service.i18n.I18nService;

@Slf4j
@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {TwinClassFreezeMode.class})
public class TwinClassFreezeDTOMapper extends RestSimpleDTOMapper<TwinClassFreezeEntity, TwinClassFreezeDTOv1> {

    @MapperModePointerBinding(modes = {StatusMode.TwinClassFreeze2StatusMode.class})
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    private final I18nService i18nService;

    @Override
    public void map(TwinClassFreezeEntity src, TwinClassFreezeDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinClassFreezeMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey())
                        .setStatusId(src.getTwinStatusId())
                        .setName(I18nCacheHolder.addId(src.getNameI18NId()))
                        .setDescription(I18nCacheHolder.addId(src.getDescriptionI18NId()))
                ;
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey());
                break;
        }

        if (mapperContext.hasModeButNot(StatusMode.TwinClassFreeze2StatusMode.HIDE)) {
            dst.setStatusId(src.getTwinStatusId());
            twinStatusRestDTOMapper.postpone(src.getTwinStatus(), mapperContext.forkOnPoint(StatusMode.Twin2StatusMode.SHORT));
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinClassFreezeMode.HIDE);
    }

    @Override
    public String getObjectCacheId(TwinClassFreezeEntity src) {
        return src.getId().toString();
    }
}
