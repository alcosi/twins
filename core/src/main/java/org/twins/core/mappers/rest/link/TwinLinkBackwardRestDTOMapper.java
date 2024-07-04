package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dto.rest.link.TwinLinkViewDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twin.TwinBaseV2RestDTOMapper;

@Component
@RequiredArgsConstructor
public class TwinLinkBackwardRestDTOMapper extends RestSimpleDTOMapper<TwinLinkEntity, TwinLinkViewDTOv1> {

    private final TwinLinkRestDTOMapper twinLinkRestDTOMapper;

    @MapperModePointerBinding(modes = MapperMode.TwinByLinkMode.class)
    private final TwinBaseV2RestDTOMapper twinBaseV2RestDTOMapper;

    @MapperModePointerBinding(modes = MapperMode.TwinLinkOnLinkMode.class)
    private final LinkBackwardRestDTOMapper linkBackwardRestDTOMapper;

    @Override
    public void map(TwinLinkEntity src, TwinLinkViewDTOv1 dst, MapperContext mapperContext) throws Exception {
        twinLinkRestDTOMapper.map(src, dst, mapperContext);
        dst
                .setDstTwinId(src.getSrcTwinId());
        if (mapperContext.hasModeButNot(MapperMode.TwinByLinkMode.WHITE))
            dst
                    .setDstTwin(twinBaseV2RestDTOMapper.convertOrPostpone(src.getSrcTwin(), mapperContext
                            .forkOnPoint(MapperMode.TwinByLinkMode.GREEN)));
        if (mapperContext.hasModeButNot(MapperMode.TwinLinkOnLinkMode.HIDE))
            dst
                    .setLink(linkBackwardRestDTOMapper.convertOrPostpone(src.getLink(), mapperContext
                            .forkOnPoint(MapperMode.TwinLinkOnLinkMode.SHORT)));
    }

    @Override
    public String getObjectCacheId(TwinLinkEntity src) {
        return src.getId().toString() + "-backward"; //postfix is important, forward and backward object are different, and should not have same objectCacheId
    }
}
