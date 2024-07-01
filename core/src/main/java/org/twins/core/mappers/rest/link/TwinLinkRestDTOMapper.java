package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dto.rest.link.TwinLinkViewDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = MapperMode.TwinLinkMode.class)
public class TwinLinkRestDTOMapper extends RestSimpleDTOMapper<TwinLinkEntity, TwinLinkViewDTOv1> {
    final UserRestDTOMapper userDTOMapper;
    @Override
    public void map(TwinLinkEntity src, TwinLinkViewDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(MapperMode.TwinLinkMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setCreatedByUserId(src.getCreatedByUserId())
                        .setCreatedByUser(userDTOMapper.convertOrPostpone(src.getCreatedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(MapperMode.TwinLinkUserMode.SHORT))))
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                        .setLinkId(src.getLinkId());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setLinkId(src.getLinkId());
                break;
        }
    }

    @Override
    public String getObjectCacheId(TwinLinkEntity src) {
        return src.getId().toString();
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(MapperMode.TwinLinkOnLinkMode.HIDE);
    }
}
