package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionGrantAssigneePropagationEntity;
import org.twins.core.dto.rest.permission.PermissionGrantAssigneePropagationSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class PermissionGrantAssigneePropagationSaveDTOReverseMapper extends RestSimpleDTOMapper<PermissionGrantAssigneePropagationSaveDTOv1, PermissionGrantAssigneePropagationEntity> {
    @Override
    public void map(PermissionGrantAssigneePropagationSaveDTOv1 src, PermissionGrantAssigneePropagationEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setPermissionSchemaId(src.getPermissionSchemaId())
                .setPermissionId(src.getPermissionId())
                .setPropagationByTwinClassId(src.getPropagationByTwinClassId())
                .setPropagationByTwinStatusId(src.getPropagationByTwinStatusId())
                .setInSpaceOnly(src.getInSpaceOnly());
    }
}
