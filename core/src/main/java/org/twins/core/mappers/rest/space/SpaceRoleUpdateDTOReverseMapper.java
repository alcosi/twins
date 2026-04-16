package org.twins.core.mappers.rest.space;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.space.SpaceRoleUpdate;
import org.twins.core.dto.rest.space.SpaceRoleUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class SpaceRoleUpdateDTOReverseMapper extends RestSimpleDTOMapper<SpaceRoleUpdateDTOv1, SpaceRoleUpdate> {
    private final SpaceRoleSaveDTOReverseMapper spaceRoleSaveDTOReverseMapper;

    @Override
    public void map(SpaceRoleUpdateDTOv1 src, SpaceRoleUpdate dst, MapperContext mapperContext) throws Exception {
        spaceRoleSaveDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }
}
