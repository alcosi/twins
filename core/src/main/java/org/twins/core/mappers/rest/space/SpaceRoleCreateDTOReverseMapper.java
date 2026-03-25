package org.twins.core.mappers.rest.space;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.space.SpaceRoleCreate;
import org.twins.core.dto.rest.space.SpaceRoleCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class SpaceRoleCreateDTOReverseMapper extends RestSimpleDTOMapper<SpaceRoleCreateDTOv1, SpaceRoleCreate> {
    private final SpaceRoleSaveDTOReverseMapper spaceRoleSaveDTOReverseMapper;

    @Override
    public void map(SpaceRoleCreateDTOv1 src, SpaceRoleCreate dst, MapperContext mapperContext) throws Exception {
        spaceRoleSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}
