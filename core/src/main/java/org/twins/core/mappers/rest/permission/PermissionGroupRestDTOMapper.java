package org.twins.core.mappers.rest.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionGroupEntity;
import org.twins.core.dto.rest.permission.PermissionGroupDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

@Component
public class PermissionGroupRestDTOMapper extends RestSimpleDTOMapper<PermissionGroupEntity, PermissionGroupDTOv1> {
    @Override
    public void map(PermissionGroupEntity src, PermissionGroupDTOv1 dst, MapperContext mapperContext) {
        switch (mapperContext.getModeOrUse(Mode.DETAILED)) {
            case DETAILED:
                dst
                        .id(src.getId())
                        .key(src.getKey())
                        .twinClassId(src.getTwinClassId())
                        .name(src.getName())
                        .description(src.getDescription());
                break;
            case SHORT:
                dst
                        .id(src.getId())
                        .key(src.getKey());
                break;
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(Mode.HIDE);
    }

    @Override
    public String getObjectCacheId(PermissionGroupEntity src) {
        return src.getId().toString();
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
