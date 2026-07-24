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
import org.twins.core.mappers.rest.mappercontext.modes.TwinLinkMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.twin.TwinBaseRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.link.TwinLinkService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class TwinLinkBackwardRestDTOMapper extends RestSimpleDTOMapper<TwinLinkEntity, TwinLinkViewDTOv1> {

    private final TwinLinkService twinLinkService;

    @MapperModePointerBinding(modes = UserMode.TwinLink2UserMode.class)
    private final UserRestDTOMapper userDTOMapper;

    @MapperModePointerBinding(modes = RelationTwinMode.TwinByLinkMode.class)
    private final TwinBaseRestDTOMapper twinBaseV2RestDTOMapper;

    @MapperModePointerBinding(modes = LinkMode.TwinLink2LinkMode.class)
    private final LinkBackwardRestDTOMapper linkBackwardRestDTOMapper;

    @Override
    public void map(TwinLinkEntity src, TwinLinkViewDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinLinkMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setCreatedByUserId(src.getCreatedByUserId())
                        .setCreatedAt(src.getCreatedAt() != null ? src.getCreatedAt().toLocalDateTime() : null)
                        .setLinkId(src.getLinkId());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setLinkId(src.getLinkId());
                break;
        }
        if (mapperContext.hasModeButNot(UserMode.TwinLink2UserMode.HIDE)) {
            dst.setCreatedByUserId(src.getCreatedByUserId());
            twinLinkService.loadCreatedByUser(src);
            userDTOMapper.convertOrPostpone(src.getCreatedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.TwinLink2UserMode.SHORT)));
        }
        dst
                .setDstTwinId(src.getSrcTwinId());
        if (mapperContext.hasModeButNot(RelationTwinMode.TwinByLinkMode.WHITE)) {
            twinLinkService.loadSrcTwin(src);
            twinBaseV2RestDTOMapper.postpone(src.getSrcTwin(), mapperContext.forkOnPoint(RelationTwinMode.TwinByLinkMode.GREEN));
        }
        if (mapperContext.hasModeButNot(LinkMode.TwinLink2LinkMode.HIDE)) {
            dst.setLinkId(src.getLinkId());
            twinLinkService.loadLink(src);
            linkBackwardRestDTOMapper.postpone(src.getLink(), mapperContext.forkOnPoint(LinkMode.TwinLink2LinkMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinLinkEntity> srcCollection, MapperContext mapperContext) throws Exception {
        if (mapperContext.hasModeButNot(UserMode.TwinLink2UserMode.HIDE)) {
            twinLinkService.loadCreatedByUser(srcCollection);
        }
        if (mapperContext.hasModeButNot(RelationTwinMode.TwinByLinkMode.WHITE)) {
            twinLinkService.loadSrcTwin(srcCollection);
        }
        if (mapperContext.hasModeButNot(LinkMode.TwinLink2LinkMode.HIDE)) {
            twinLinkService.loadLink(srcCollection);
        }
    }

    @Override
    public String getObjectCacheId(TwinLinkEntity src) {
        return src.getId().toString() + "-backward"; //postfix is important, forward and backward object are different, and should not have same objectCacheId
    }
}
