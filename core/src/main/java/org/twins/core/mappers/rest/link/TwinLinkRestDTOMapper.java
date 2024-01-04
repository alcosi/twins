package org.twins.core.mappers.rest.link;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dto.rest.link.TwinLinkViewDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

@Component
@RequiredArgsConstructor
public class TwinLinkRestDTOMapper extends RestSimpleDTOMapper<TwinLinkEntity, TwinLinkViewDTOv1> {
    final UserRestDTOMapper userDTOMapper;
    @Override
    public void map(TwinLinkEntity src, TwinLinkViewDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(Mode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setCreatedByUserId(src.getCreatedByUserId())
                        .setCreatedByUser(userDTOMapper.convertOrPostpone(src.getCreatedByUser(), mapperContext.cloneWithIsolatedModes().setMode(UserRestDTOMapper.Mode.SHORT)))
                        .setCreatedAt(src.getCreatedAt().toInstant())
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
        return mapperContext.hasModeOrEmpty(Mode.HIDE);
    }

    @AllArgsConstructor
    public enum Mode implements MapperMode {
        HIDE(0),
        SHORT(1),
        DETAILED(2);

        public static final String _HIDE = "HIDE";
        public static final String _SHORT = "SHORT";
        public static final String _DETAILED = "DETAILED";

        @Getter
        final int priority;
    }
}
