package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dto.rest.link.TwinLinkDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinLinkMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinLinkMode.class)
public class TwinLinkRestDTOMapper extends RestSimpleDTOMapper<TwinLinkEntity, TwinLinkDTOv1> {
    @MapperModePointerBinding(modes = UserMode.TwinLink2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @Override
    public void map(TwinLinkEntity src, TwinLinkDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinLinkMode.DETAILED)) {
            case DETAILED -> dst
                    .setId(src.getId())
                    .setLinkId(src.getLinkId())
                    .setCreatedByUserId(src.getCreatedByUserId())
                    .setCreatedAt(src.getCreatedAt().toLocalDateTime());
            case SHORT -> dst
                    .setId(src.getId())
                    .setLinkId(src.getLinkId());
        }
        if (mapperContext.hasModeButNot(UserMode.TwinLink2UserMode.HIDE)) {
            dst.setCreatedByUserId(src.getCreatedByUserId());
            userRestDTOMapper.postpone(src.getCreatedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.TwinLink2UserMode.SHORT)));
        }
    }

    @Override
    public String getObjectCacheId(TwinLinkEntity src) {
        return src.getId().toString();
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinLinkMode.HIDE);
    }
}
