package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dto.rest.link.TwinLinkViewDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.LinkMode;
import org.twins.core.mappers.rest.mappercontext.modes.RelationTwinMode;
import org.twins.core.mappers.rest.twin.TwinBaseRestDTOMapper;

@Component
@RequiredArgsConstructor
public class TwinLinkForwardRestDTOMapper extends RestSimpleDTOMapper<TwinLinkEntity, TwinLinkViewDTOv1> {

    private final TwinLinkRestDTOMapper twinLinkRestDTOMapper;

    @MapperModePointerBinding(modes = RelationTwinMode.TwinByLinkMode.class)
    private final TwinBaseRestDTOMapper twinBaseRestDTOMapper;

    @MapperModePointerBinding(modes = LinkMode.TwinLink2LinkMode.class)
    private final LinkForwardRestDTOMapper linkForwardRestDTOMapper;

    @Override
    public void map(TwinLinkEntity src, TwinLinkViewDTOv1 dst, MapperContext mapperContext) throws Exception {
        twinLinkRestDTOMapper.map(src, dst, mapperContext);
        dst
                .setDstTwinId(src.getDstTwinId());
        if (mapperContext.hasModeButNot(RelationTwinMode.TwinByLinkMode.WHITE)) {
            twinBaseRestDTOMapper.postpone(src.getDstTwin(), mapperContext.forkOnPoint(RelationTwinMode.TwinByLinkMode.GREEN));
        }
        if (mapperContext.hasModeButNot(LinkMode.TwinLink2LinkMode.HIDE)) {
            linkForwardRestDTOMapper.postpone(src.getLink(), mapperContext.forkOnPoint(LinkMode.TwinLink2LinkMode.SHORT));
        }
    }

    @Override
    public String getObjectCacheId(TwinLinkEntity src) {
        return src.getId().toString() + "-forward"; //postfix is important, forward and backward object are different, and should not have same objectCacheId
    }
}
