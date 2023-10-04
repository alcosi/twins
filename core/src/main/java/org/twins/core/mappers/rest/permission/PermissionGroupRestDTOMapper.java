package org.twins.core.mappers.rest.permission;

import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionGroupEntity;
import org.twins.core.dto.rest.permission.PermissionGroupDTOv1;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

@Component
public class PermissionGroupRestDTOMapper extends RestSimpleDTOMapper<PermissionGroupEntity, PermissionGroupDTOv1> {
    @Override
    public void map(PermissionGroupEntity src, PermissionGroupDTOv1 dst, MapperProperties mapperProperties) {
        switch (mapperProperties.getModeOrUse(Mode.DETAILED)) {
            case DETAILED:
                dst
                        .twinClassId(src.getTwinClassId());
            default:
                dst
                        .id(src.getId())
                        .name(src.getName());
        }
    }

    public enum Mode implements MapperMode {
        ID_ONLY, DETAILED;

        public static final String _ID_ONLY = "ID_ONLY";
        public static final String _DETAILED = "DETAILED";
    }
}
