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
import org.twins.core.service.link.LinkService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = LinkMode.class)
public class LinkBackwardRestDTOMapper extends RestSimpleDTOMapper<LinkEntity, LinkDTOv1> {

    @Lazy
    @Autowired
    @MapperModePointerBinding(modes = TwinClassMode.LinkDst2TwinClassMode.class)
    private TwinClassRestDTOMapper twinClassRestDTOMapper;

    private final LinkService linkService;

    @Override
    public void map(LinkEntity src, LinkDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(LinkMode.DETAILED)) {
            case DETAILED, MANAGED:
                dst
                        .setId(src.getId())
                        .setName(I18nCacheHolder.addId(src.getBackwardNameI18NId()))
                        .setDstTwinClassId(src.getSrcTwinClassId())
                        .setLinkStrengthId(src.getLinkStrengthId())
                        .setType(src.getType());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setName(I18nCacheHolder.addId(src.getBackwardNameI18NId()));
        }

        if (mapperContext.hasModeButNot(TwinClassMode.LinkDst2TwinClassMode.HIDE)) {
            linkService.loadTwinClasses(src);
            dst.setDstTwinClassId(src.getSrcTwinClassId());
            twinClassRestDTOMapper.convertOrPostpone(src.getSrcTwinClass(), mapperContext.forkOnPoint(TwinClassMode.LinkDst2TwinClassMode.SHORT));
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(LinkMode.HIDE);
    }

    @Override
    public String getObjectCacheId(LinkEntity src) {
        return src.getId().toString() + "-backward"; //postfix is important, forward and backward object are different, and should not have same objectCacheId
    }

    @Override
    public void beforeCollectionConversion(Collection<LinkEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(TwinClassMode.LinkDst2TwinClassMode.HIDE)) {
            linkService.loadTwinClasses(srcCollection);
        }
    }
}
