package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.PermissionGrantAssigneePropagationSearch;
import org.twins.core.dto.rest.permission.PermissionGrantAssigneePropagationSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class PermissionGrantAssigneePropagationSearchDTOReverseMapper extends RestSimpleDTOMapper<PermissionGrantAssigneePropagationSearchRqDTOv1, PermissionGrantAssigneePropagationSearch> {

    @Override
    public void map(PermissionGrantAssigneePropagationSearchRqDTOv1 src, PermissionGrantAssigneePropagationSearch dst, MapperContext mapperContext) {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setPermissionSchemaIdList(src.getPermissionSchemaIdList())
                .setPermissionSchemaIdExcludeList(src.getPermissionSchemaIdExcludeList())
                .setPermissionIdList(src.getPermissionIdList())
                .setPermissionIdExcludeList(src.getPermissionIdExcludeList())
                .setPropagationTwinClassIdList(src.getPropagationTwinClassIdList())
                .setPropagationTwinClassIdExcludeList(src.getPropagationTwinClassIdExcludeList())
                .setPropagationTwinStatusIdList(src.getPropagationTwinStatusIdList())
                .setPropagationTwinStatusIdExcludeList(src.getPropagationTwinStatusIdExcludeList())
                .setGrantedByUserIdList(src.getGrantedByUserIdList())
                .setGrantedByUserIdExcludeList(src.getGrantedByUserIdExcludeList());
    }
}
