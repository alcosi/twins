package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dto.rest.link.TwinLinkDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.LinkMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinMode;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;

@Component
@RequiredArgsConstructor
public class TwinLinkBaseRestDTOMapper extends RestSimpleDTOMapper<TwinLinkEntity, TwinLinkDTOv1> {

    private final TwinLinkRestDTOMapper twinLinkRestDTOMapper;

    @MapperModePointerBinding(modes = TwinMode.TwinLink2TwinMode.class)
    private final TwinRestDTOMapperV2 twinRestDTOMapperV2;

    @MapperModePointerBinding(modes = LinkMode.TwinLink2LinkMode.class)
    private final LinkBackwardRestDTOMapper linkBackwardRestDTOMapper;

    @Override
    public void map(TwinLinkEntity src, TwinLinkDTOv1 dst, MapperContext mapperContext) throws Exception {
        twinLinkRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(TwinMode.TwinLink2TwinMode.HIDE)) {
            dst
                    .setSrcTwinId(src.getSrcTwinId())
                    .setDstTwinId(src.getDstTwinId());
            twinRestDTOMapperV2.postpone(src.getSrcTwin(), mapperContext.forkOnPoint(TwinMode.TwinLink2TwinMode.SHORT));
            twinRestDTOMapperV2.postpone(src.getDstTwin(), mapperContext.forkOnPoint(TwinMode.TwinLink2TwinMode.SHORT));
        }
        if (mapperContext.hasModeButNot(LinkMode.TwinLink2LinkMode.HIDE)) {
            linkBackwardRestDTOMapper.postpone(src.getLink(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(LinkMode.TwinLink2LinkMode.SHORT)));
        }
    }
}
