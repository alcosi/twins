package org.twins.core.mappers.rest.permission;

import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.permission.PermissionGrantAssigneePropagationEntity;
import org.twins.core.dto.rest.permission.PermissionGrantAssigneePropagationDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionGrantAssigneePropagationMode;

@Component
@MapperModeBinding(modes = PermissionGrantAssigneePropagationMode.class)
public class PermissionGrantAssigneePropagationRestDTOMapper extends RestSimpleDTOMapper<PermissionGrantAssigneePropagationEntity, PermissionGrantAssigneePropagationDTOv1> {

    @Override
    public void map(PermissionGrantAssigneePropagationEntity src, PermissionGrantAssigneePropagationDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(PermissionGrantAssigneePropagationMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setPermissionSchemaId(src.getPermissionSchemaId())
                        .setPermissionId(src.getPermissionId())
                        .setPropagationTwinClassId(src.getPropagationByTwinClassId())
                        .setPropagationTwinStatusId(src.getPropagationByTwinStatusId())
                        .setGrantedByUserId(src.getGrantedByUserId())
                        .setGrantedAt(src.getGrantedAt().toLocalDateTime());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setPermissionId(src.getPermissionId())
                        .setPropagationTwinClassId(src.getPropagationByTwinClassId());
                break;
        }
    }
}
