package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.KitUtils;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dto.rest.link.TwinLinkListDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.service.link.TwinLinkService;

import java.util.LinkedHashMap;


@Component
@RequiredArgsConstructor
public class TwinLinkListRestDTOMapper extends RestSimpleDTOMapper<TwinLinkService.FindTwinLinksResult, TwinLinkListDTOv1> {
    final TwinLinkForwardRestDTOMapper twinLinkForwardRestDTOMapper;
    final TwinLinkBackwardRestDTOMapper twinLinkBackwardRestDTOMapper;

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinLinkRestDTOMapper.Mode.HIDE);
    }

    @Override
    public void map(TwinLinkService.FindTwinLinksResult src, TwinLinkListDTOv1 dst, MapperContext mapperContext) throws Exception {
        if (KitUtils.isNotEmpty(src.getForwardLinks())) {
            dst.forwardLinks = new LinkedHashMap<>();
            for (TwinLinkEntity link : src.getForwardLinks().getList()) {
                dst.forwardLinks.put(link.getId(), twinLinkForwardRestDTOMapper.convert(link, mapperContext));
            }
        }
        if (KitUtils.isNotEmpty(src.getBackwardLinks())) {
            dst.backwardLinks = new LinkedHashMap<>();
            for (TwinLinkEntity link : src.getBackwardLinks().getList()) {
                dst.backwardLinks.put(link.getId(), twinLinkBackwardRestDTOMapper.convert(link, mapperContext));
            }
        }
    }
}
