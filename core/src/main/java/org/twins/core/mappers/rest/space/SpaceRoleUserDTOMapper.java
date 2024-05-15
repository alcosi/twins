package org.twins.core.mappers.rest.space;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dto.rest.space.SpaceRoleUserDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class SpaceRoleUserDTOMapper extends RestSimpleDTOMapper<SpaceRoleUserEntity, SpaceRoleUserDTOv1> {
    final SpaceRoleByUserDTOMapper spaceRoleByUserDTOMapper;
    @Override
    public void map(SpaceRoleUserEntity src, SpaceRoleUserDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(Mode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setTwinId(src.getTwinId())
                        .setSpaceRoleId(src.getSpaceRoleId())
                        .setUserId(src.getUserId())
                        .setCreatedByUserId(src.getCreatedByUserId())
                        .setSpaceRole(spaceRoleByUserDTOMapper.convert(src, mapperContext));
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setTwinId(src.getTwinId())
                        .setSpaceRoleId(src.getSpaceRoleId())
                        .setUserId(src.getUserId());
                break;
        }
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

    @Override
    public String getObjectCacheId(SpaceRoleUserEntity src) {
        return src.getId().toString();
    }
}
