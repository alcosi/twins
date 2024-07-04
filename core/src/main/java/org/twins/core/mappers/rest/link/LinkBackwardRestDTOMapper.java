package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dto.rest.link.LinkDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassBaseRestDTOMapper;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = MapperMode.LinkMode.class)
public class LinkBackwardRestDTOMapper extends RestSimpleDTOMapper<LinkEntity, LinkDTOv1> {

    @MapperModePointerBinding(modes = MapperMode.LinkDstClassMode.class)
    private final TwinClassBaseRestDTOMapper twinClassBaseRestDTOMapper;

    private final I18nService i18nService;

    @Override
    public void map(LinkEntity src, LinkDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(MapperMode.LinkMode.DETAILED)) {
            case DETAILED:
                dst
                        .dstTwinClassId(src.getSrcTwinClassId())
                        .linkStrengthId(src.getLinkStrengthId())
                        .type(src.getType());
            case SHORT:
                dst
                        .id(src.getId())
                        .name(i18nService.translateToLocale(src.getBackwardNameI18NId()));
        }
        if (mapperContext.hasModeButNot(MapperMode.LinkMode.HIDE))
            dst
                    .dstTwinClass(twinClassBaseRestDTOMapper.convertOrPostpone(src.getSrcTwinClass(), mapperContext
                            .forkOnPoint(MapperMode.LinkDstClassMode.SHORT)));
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(MapperMode.LinkMode.HIDE);
    }

    @Override
    public String getObjectCacheId(LinkEntity src) {
        return src.getId().toString() + "-backward"; //postfix is important, forward and backward object are different, and should not have same objectCacheId
    }
}
