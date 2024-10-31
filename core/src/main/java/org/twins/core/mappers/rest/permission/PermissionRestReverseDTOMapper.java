package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dto.rest.permission.PermissionSaveRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class PermissionRestReverseDTOMapper extends RestSimpleDTOMapper<PermissionSaveRqDTOv1, PermissionEntity> {

    @Override
    public void map(PermissionSaveRqDTOv1 src, PermissionEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setKey(src.getKey())
                .setPermissionGroupId(src.getGroupId());
    }
}
