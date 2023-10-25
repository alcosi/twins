package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dto.rest.link.TwinLinkListDTOv1;
import org.twins.core.dto.rest.link.TwinLinkViewDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twin.TwinFieldRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;


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
        if (MapUtils.isNotEmpty(src.getForwardLinks())) {
            dst.forwardLinks = new LinkedHashMap<>();
            for (Map.Entry<UUID, TwinLinkEntity> entry : src.getForwardLinks().entrySet()) {
                dst.forwardLinks.put(entry.getKey(), twinLinkForwardRestDTOMapper.convert(entry.getValue(), mapperContext));
            }
        }
        if (MapUtils.isNotEmpty(src.getBackwardLinks())) {
            dst.backwardLinks = new LinkedHashMap<>();
            for (Map.Entry<UUID, TwinLinkEntity> entry : src.getBackwardLinks().entrySet()) {
                dst.backwardLinks.put(entry.getKey(), twinLinkBackwardRestDTOMapper.convert(entry.getValue(), mapperContext));
            }
        }
    }
}
