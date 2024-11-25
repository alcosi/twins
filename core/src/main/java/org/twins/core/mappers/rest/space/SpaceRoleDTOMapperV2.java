package org.twins.core.mappers.rest.space;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.space.SpaceRoleEntity;
import org.twins.core.dto.rest.space.SpaceRoleDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.businessaccount.BusinessAccountDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.BusinessAccountMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;


@Component
@RequiredArgsConstructor
public class SpaceRoleDTOMapperV2 extends RestSimpleDTOMapper<SpaceRoleEntity, SpaceRoleDTOv2> {

    private final SpaceRoleDTOMapper spaceRoleDTOMapper;

    @MapperModePointerBinding(modes = TwinClassMode.SpaceRole2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @MapperModePointerBinding(modes = BusinessAccountMode.SpaceRole2BusinessAccountMode.class)
    private final BusinessAccountDTOMapper businessAccountDTOMapper;

    @Override
    public void map(SpaceRoleEntity src, SpaceRoleDTOv2 dst, MapperContext mapperContext) throws Exception {
        spaceRoleDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(TwinClassMode.SpaceRole2TwinClassMode.HIDE))
            dst
                    .setTwinClass(twinClassRestDTOMapper.convertOrPostpone(src.getTwinClass(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassMode.SpaceRole2TwinClassMode.SHORT))))
                    .setTwinClassId(src.getTwinClassId());
        if (mapperContext.hasModeButNot(BusinessAccountMode.SpaceRole2BusinessAccountMode.HIDE))
            dst
                    .setBusinessAccount(businessAccountDTOMapper.convertOrPostpone(src.getBusinessAccount(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(BusinessAccountMode.SpaceRole2BusinessAccountMode.SHORT))))
                    .setBusinessAccountId(src.getBusinessAccountId());
    }
}
