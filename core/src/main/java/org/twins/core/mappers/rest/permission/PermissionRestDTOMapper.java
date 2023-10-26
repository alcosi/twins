package org.twins.core.mappers.rest.permission;

import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dto.rest.permission.PermissionDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

@Component
public class PermissionRestDTOMapper extends RestSimpleDTOMapper<PermissionEntity, PermissionDTOv1> {
    @Override
    public void map(PermissionEntity src, PermissionDTOv1 dst, MapperContext mapperContext) {
        switch (mapperContext.getModeOrUse(Mode.DETAILED)) {
            case DETAILED:
                dst
                        .id(src.getId())
                        .key(src.getKey())
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
    public String getObjectCacheId(PermissionEntity src) {
        return src.getId().toString();
    }

    public enum Mode implements MapperMode {
        SHORT, DETAILED, HIDE;

        public static final String _SHORT = "SHORT";
        public static final String _DETAILED = "DETAILED";
        public static final String _HIDE = "HIDE";
    }
}
