package org.twins.core.mappers.rest.permission;

import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionGroupEntity;
import org.twins.core.dto.rest.permission.PermissionGroupDTOv1;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

@Component
public class PermissionGroupRestDTOMapper extends RestSimpleDTOMapper<PermissionGroupEntity, PermissionGroupDTOv1> {
    @Override
    public void map(PermissionGroupEntity src, PermissionGroupDTOv1 dst, MapperContext mapperContext) {
        switch (mapperContext.getModeOrUse(Mode.DETAILED)) {
            case DETAILED:
                dst
                        .twinClassId(src.getTwinClassId())
                        .name(src.getName())
                        .description(src.getDescription());
            default:
                dst
                        .id(src.getId())
                        .key(src.getKey());
        }
    }

    public enum Mode implements MapperMode {
        ID_KEY_ONLY, DETAILED;

        public static final String _ID_KEY_ONLY = "ID_KEY_ONLY";
        public static final String _DETAILED = "DETAILED";
    }
}
