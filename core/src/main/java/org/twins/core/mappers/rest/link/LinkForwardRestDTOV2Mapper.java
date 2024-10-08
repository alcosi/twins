package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dto.rest.link.LinkDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.LinkMode;

@Component
@RequiredArgsConstructor
public class LinkForwardRestDTOV2Mapper extends RestSimpleDTOMapper<LinkEntity, LinkDTOv2> {

    private final LinkForwardRestDTOMapper linkForwardRestDTOMapper;

    private final I18nService i18nService;

    @Override
    public void map(LinkEntity src, LinkDTOv2 dst, MapperContext mapperContext) throws Exception {
        linkForwardRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasMode(LinkMode.DETAILED))
            dst
                    .srcTwinClassId(src.getSrcTwinClassId())
                    .backwardName(i18nService.translateToLocale(src.getBackwardNameI18NId()))
                    .createdByUserId(src.getCreatedByUserId())
                    .createdAt(src.getCreatedAt().toLocalDateTime());
    }
}
