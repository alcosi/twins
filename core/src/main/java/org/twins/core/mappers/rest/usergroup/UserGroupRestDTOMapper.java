package org.twins.core.mappers.rest.usergroup;

import org.springframework.stereotype.Component;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dto.rest.usergroup.UserGroupDTOv1;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

@Component
public class UserGroupRestDTOMapper extends RestSimpleDTOMapper<UserGroupEntity, UserGroupDTOv1> {
    @Override
    public void map(UserGroupEntity src, UserGroupDTOv1 dst, MapperContext mapperContext) {
        switch (mapperContext.getModeOrUse(Mode.DETAILED)) {
            case ID_ONLY:
                dst
                        .id(src.getId());
                break;
            case DETAILED:
                dst
                        .id(src.getId())
                        .name(src.getName())
                        .businessAccountId(src.getBusinessAccountId())
                        .type(src.getUserGroupTypeId());
                break;
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
