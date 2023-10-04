package org.twins.core.mappers.rest.permission;

import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dto.rest.permission.PermissionDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupDTOv1;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

@Component
public class PermissionRestDTOMapper extends RestSimpleDTOMapper<PermissionEntity, PermissionDTOv1> {
    @Override
    public void map(PermissionEntity src, PermissionDTOv1 dst, MapperProperties mapperProperties) {
        switch (mapperProperties.getModeOrUse(Mode.DETAILED)) {
            case DETAILED:
                dst
                        .name(src.getName())
                        .description(src.getDescription());
            default:
                dst
                        .id(src.getId())
                        .key(src.getKey());
        }
    }

    public enum Mode implements MapperMode {
        ID_ONLY, DETAILED;

        public static final String _ID_ONLY = "ID_ONLY";
        public static final String _DETAILED = "DETAILED";
    }
}
