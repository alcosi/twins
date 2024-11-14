package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dto.rest.permission.PermissionSchemaDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.businessaccount.BusinessAccountDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.BusinessAccountMode;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionSchemaMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = PermissionSchemaMode.class)
public class PermissionSchemaRestDTOMapperV2 extends RestSimpleDTOMapper<PermissionSchemaEntity, PermissionSchemaDTOv2> {

    private final PermissionSchemaRestDTOMapper permissionSchemaRestDTOMapper;
    @MapperModePointerBinding(modes = BusinessAccountMode.PermissionSchema2BusinessAccountMode.class)
    private final BusinessAccountDTOMapper businessAccountDTOMapper;
    @MapperModePointerBinding(modes = UserMode.PermissionSchema2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @Override
    public void map(PermissionSchemaEntity src, PermissionSchemaDTOv2 dst, MapperContext mapperContext) throws Exception {
        permissionSchemaRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(BusinessAccountMode.PermissionSchema2BusinessAccountMode.HIDE))
            dst
                    .setBusinessAccount(businessAccountDTOMapper.convertOrPostpone(src.getBusinessAccount(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(BusinessAccountMode.PermissionSchema2BusinessAccountMode.SHORT))))
                    .setBusinessAccountId(src.getBusinessAccountId());
        if (mapperContext.hasModeButNot(UserMode.PermissionSchema2UserMode.HIDE))
            dst
                    .setCreatedByUser(userRestDTOMapper.convertOrPostpone(src.getCreatedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.PermissionSchema2UserMode.SHORT))))
                    .setCreatedByUserId(src.getCreatedByUserId());
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
