package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionGrantAssigneePropagationEntity;
import org.twins.core.dto.rest.permission.PermissionGrantAssigneePropagationCreateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class PermissionGrantAssigneePropagationCreateDTOReverseMapper extends RestSimpleDTOMapper<PermissionGrantAssigneePropagationCreateRqDTOv1, PermissionGrantAssigneePropagationEntity> {
    private final PermissionGrantAssigneePropagationSaveDTOReverseMapper permissionGrantAssigneePropagationSaveDTOReverseMapper;

    @Override
    public void map(PermissionGrantAssigneePropagationCreateRqDTOv1 src, PermissionGrantAssigneePropagationEntity dst, MapperContext mapperContext) throws Exception {
        permissionGrantAssigneePropagationSaveDTOReverseMapper.map(src.getPermissionGrantAssigneePropagation(), dst, mapperContext);
    }
}
