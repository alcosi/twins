package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dto.rest.link.LinkDTOv1;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.LinkMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.service.i18n.I18nService;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = LinkMode.class)
public class LinkForwardRestDTOMapper extends RestSimpleDTOMapper<LinkEntity, LinkDTOv1> {

    @Lazy
    @Autowired
    @MapperModePointerBinding(modes = TwinClassMode.LinkDst2TwinClassMode.class)
    private TwinClassRestDTOMapper twinClassRestDTOMapper;

    private final I18nService i18nService;

    @Override
    public void map(LinkEntity src, LinkDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(LinkMode.DETAILED)) {
            case DETAILED, MANAGED:
                dst
                        .setId(src.getId())
                        .setName(I18nCacheHolder.addId(src.getForwardNameI18NId()))
                        .setDstTwinClassId(src.getDstTwinClassId())
                        .setLinkStrengthId(src.getLinkStrengthId())
                        .setType(src.getType());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setName(I18nCacheHolder.addId(src.getForwardNameI18NId()));
        }
        if (mapperContext.hasModeButNot(TwinClassMode.LinkDst2TwinClassMode.HIDE)) {
            dst.setDstTwinClassId(src.getDstTwinClassId());
            twinClassRestDTOMapper.convertOrPostpone(src.getDstTwinClass(), mapperContext.forkOnPoint(TwinClassMode.LinkDst2TwinClassMode.SHORT));
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(LinkMode.HIDE);
    }

    @Override
    public String getObjectCacheId(LinkEntity src) {
        return src.getId().toString() + "-forward"; //postfix is important, forward and backward object are different, and should not have same objectCacheId
    }

}
