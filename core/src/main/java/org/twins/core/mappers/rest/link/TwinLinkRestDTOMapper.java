package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dto.rest.link.TwinLinkDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.LinkMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinLinkMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.twin.TwinBaseRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.link.TwinLinkService;

import java.util.Collection;

/**
 * Standalone twin link representation (both endpoints exposed) — used by the twin_link
 * view/search APIs. Unlike {@link TwinLinkForwardRestDTOMapper} (which renders a link from the
 * perspective of the twin that owns it and therefore omits the source), this mapper always
 * exposes both {@code srcTwinId} and {@code dstTwinId} and enriches both endpoints as related twins.
 */
@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinLinkMode.class)
public class TwinLinkRestDTOMapper extends RestSimpleDTOMapper<TwinLinkEntity, TwinLinkDTOv1> {

    @MapperModePointerBinding(modes = TwinMode.TwinLink2TwinMode.class)
    private final TwinBaseRestDTOMapper twinBaseRestDTOMapper;

    @MapperModePointerBinding(modes = LinkMode.TwinLink2LinkMode.class)
    private final LinkForwardRestDTOMapper linkForwardRestDTOMapper;

    @MapperModePointerBinding(modes = UserMode.TwinLink2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    private final TwinLinkService twinLinkService;

    @Override
    public void map(TwinLinkEntity src, TwinLinkDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinLinkMode.DETAILED)) {
            case DETAILED -> dst
                    .setId(src.getId())
                    .setSrcTwinId(src.getSrcTwinId())
                    .setDstTwinId(src.getDstTwinId())
                    .setLinkId(src.getLinkId())
                    .setCreatedAt(src.getCreatedAt() != null ? src.getCreatedAt().toLocalDateTime() : null)
                    .setCreatedByUserId(src.getCreatedByUserId());
            case SHORT -> dst
                    .setId(src.getId())
                    .setSrcTwinId(src.getSrcTwinId())
                    .setDstTwinId(src.getDstTwinId())
                    .setLinkId(src.getLinkId());
        }
        if (mapperContext.hasModeButNot(TwinMode.TwinLink2TwinMode.HIDE)) {
            twinLinkService.loadTwin(src);
            twinBaseRestDTOMapper.postpone(src.getSrcTwin(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinMode.TwinLink2TwinMode.SHORT)));
            twinBaseRestDTOMapper.postpone(src.getDstTwin(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinMode.TwinLink2TwinMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(LinkMode.TwinLink2LinkMode.HIDE)) {
            twinLinkService.loadLink(src);
            linkForwardRestDTOMapper.convertOrPostpone(src.getLink(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(LinkMode.TwinLink2LinkMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(UserMode.TwinLink2UserMode.HIDE)) {
            dst.setCreatedByUserId(src.getCreatedByUserId());
            twinLinkService.loadCreatedByUser(src);
            userRestDTOMapper.convertOrPostpone(src.getCreatedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.TwinLink2UserMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinLinkEntity> srcCollection, MapperContext mapperContext) throws Exception {
        if (mapperContext.hasModeButNot(TwinMode.TwinLink2TwinMode.HIDE)) {
            twinLinkService.loadTwin(srcCollection);
        }
        if (mapperContext.hasModeButNot(LinkMode.TwinLink2LinkMode.HIDE)) {
            twinLinkService.loadLink(srcCollection);
        }
        if (mapperContext.hasModeButNot(UserMode.TwinLink2UserMode.HIDE)) {
            twinLinkService.loadCreatedByUser(srcCollection);
        }
    }

    @Override
    public String getObjectCacheId(TwinLinkEntity src) {
        return src.getId().toString();
    }
}
