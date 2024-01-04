package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dto.rest.link.LinkDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twin.RelatedTwinMode;
import org.twins.core.mappers.rest.twinclass.TwinClassBaseRestDTOMapper;

@Component
@RequiredArgsConstructor
public class LinkBackwardRestDTOMapper extends RestSimpleDTOMapper<LinkEntity, LinkDTOv1> {
    final I18nService i18nService;
    final TwinClassBaseRestDTOMapper twinClassBaseRestDTOMapper;

    @Override
    public void map(LinkEntity src, LinkDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(LinkRestDTOMapper.Mode.DETAILED)) {
            case DETAILED:
                dst
                        .dstTwinClassId(src.getSrcTwinClassId())
                        .dstTwinClass(twinClassBaseRestDTOMapper.convertOrPostpone(src.getSrcTwinClass(), mapperContext
                                .cloneWithIsolatedModes(RelatedTwinMode.GREEN)))
                        .mandatory(false)
                        .type(src.getType());
            case SHORT:
                dst
                        .id(src.getId())
                        .name(i18nService.translateToLocale(src.getBackwardNameI18NId()));
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(LinkRestDTOMapper.Mode.HIDE);
    }

    @Override
    public String getObjectCacheId(LinkEntity src) {
        return src.getId().toString() + "-backward"; //postfix is important, forward and backward object are different, and should not have same objectCacheId
    }
}
