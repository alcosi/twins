package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dto.rest.link.LinkDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassBaseRestDTOMapper;

@Component
@RequiredArgsConstructor
public class LinkBackwardRestDTOMapper extends RestSimpleDTOMapper<LinkEntity, LinkDTOv1> {
    final I18nService i18nService;
    final TwinClassBaseRestDTOMapper twinClassBaseRestDTOMapper;

    @Override
    public void map(LinkEntity src, LinkDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .id(src.getId())
                .dstTwinClass(twinClassBaseRestDTOMapper.convert(src.getSrcTwinClass(), mapperContext))
                .name(i18nService.translateToLocale(src.getBackwardNameI18n()))
                .type(src.getType());
    }
}
