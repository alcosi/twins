package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dto.rest.link.TwinLinkViewDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twin.RelatedTwinMode;
import org.twins.core.mappers.rest.twin.TwinBaseV2RestDTOMapper;

@Component
@RequiredArgsConstructor
public class TwinLinkForwardRestDTOMapper extends RestSimpleDTOMapper<TwinLinkEntity, TwinLinkViewDTOv1> {
    final TwinLinkRestDTOMapper twinLinkRestDTOMapper;
    final TwinBaseV2RestDTOMapper twinBaseV2RestDTOMapper;
    final LinkForwardRestDTOMapper linkForwardRestDTOMapper;
    @Override
    public void map(TwinLinkEntity src, TwinLinkViewDTOv1 dst, MapperContext mapperContext) throws Exception {
        twinLinkRestDTOMapper.map(src, dst, mapperContext);
        dst
                .setDstTwin(twinBaseV2RestDTOMapper.convertOrPostpone(src.getDstTwin(), mapperContext
                        .cloneWithIsolatedModes(RelatedTwinMode.GREEN)))
                .setLink(linkForwardRestDTOMapper.convert(src.getLink(), mapperContext))
                .setDstTwinId(src.getDstTwinId());
    }

    @Override
    public String getObjectCacheId(TwinLinkEntity src) {
        return src.getId().toString() + "-forward"; //postfix is important, forward and backward object are different, and should not have same objectCacheId
    }
}
