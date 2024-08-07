package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dto.rest.link.LinkDTOv1;
import org.twins.core.mappers.rest.mappercontext.modes.LinkMode;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.twinclass.TwinClassBaseRestDTOMapper;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = LinkMode.class)
public class LinkForwardRestDTOMapper extends RestSimpleDTOMapper<LinkEntity, LinkDTOv1> {

    @MapperModePointerBinding(modes = TwinClassMode.LinkDst2TwinClassMode.class)
    private final TwinClassBaseRestDTOMapper twinClassBaseRestDTOMapper;

    private final I18nService i18nService;

    @Override
    public void map(LinkEntity src, LinkDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(LinkMode.DETAILED)) {
            case DETAILED:
                dst
                        .dstTwinClassId(src.getDstTwinClassId())
                        .linkStrengthId(src.getLinkStrengthId())
                        .type(src.getType());
            case SHORT:
                dst
                        .id(src.getId())
                        .name(i18nService.translateToLocale(src.getForwardNameI18NId()));
        }

        if (mapperContext.hasModeButNot(LinkMode.HIDE))
            dst
                    .dstTwinClass(twinClassBaseRestDTOMapper.convertOrPostpone(src.getDstTwinClass(), mapperContext
                            .forkOnPoint(TwinClassMode.LinkDst2TwinClassMode.SHORT)));
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
