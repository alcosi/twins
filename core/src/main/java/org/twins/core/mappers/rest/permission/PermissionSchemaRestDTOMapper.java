package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dto.rest.permission.PermissionSchemaDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.businessaccount.BusinessAccountDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.BusinessAccountMode;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionSchemaMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

import static org.cambium.common.util.DateUtils.convertOrNull;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = PermissionSchemaMode.class)
public class PermissionSchemaRestDTOMapper extends RestSimpleDTOMapper<PermissionSchemaEntity, PermissionSchemaDTOv1> {
    @MapperModePointerBinding(modes = BusinessAccountMode.PermissionSchema2BusinessAccountMode.class)
    private final BusinessAccountDTOMapper businessAccountDTOMapper;

    @MapperModePointerBinding(modes = UserMode.PermissionSchema2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @Override
    public void map(PermissionSchemaEntity src, PermissionSchemaDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(PermissionSchemaMode.DETAILED)) {
            case DETAILED -> dst
                    .setId(src.getId())
                    .setName(src.getName())
                    .setDomainId(src.getDomainId())
                    .setBusinessAccountId(src.getBusinessAccountId())
                    .setDescription(src.getDescription())
                    .setCreatedAt(convertOrNull(src.getCreatedAt()))
                    .setCreatedByUserId(src.getCreatedByUserId());
            case SHORT -> dst
                    .setId(src.getId())
                    .setName(src.getName());
        }

        if (mapperContext.hasModeButNot(BusinessAccountMode.PermissionSchema2BusinessAccountMode.HIDE)) {
            dst.setBusinessAccountId(src.getBusinessAccountId());
            businessAccountDTOMapper.postpone(src.getBusinessAccount(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(BusinessAccountMode.PermissionSchema2BusinessAccountMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(UserMode.PermissionSchema2UserMode.HIDE)) {
            dst.setCreatedByUserId(src.getCreatedByUserId());
            userRestDTOMapper.postpone(src.getCreatedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.PermissionSchema2UserMode.SHORT)));
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(PermissionSchemaMode.HIDE);
    }

    @Override
    public String getObjectCacheId(PermissionSchemaEntity src) {
        return src.getId().toString();
    }
}
