package org.twins.core.mappers.rest.space;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.space.SpaceRoleEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.space.SpaceRoleCountDTOv1;
import org.twins.core.enums.sort.SpaceRoleGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.businessaccount.BusinessAccountDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.BusinessAccountMode;
import org.twins.core.mappers.rest.mappercontext.modes.SpaceRoleMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.service.space.SpaceRoleService;

import java.util.Collection;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = SpaceRoleMode.class)
public class SpaceRoleCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<SpaceRoleEntity, SpaceRoleGroupField>, SpaceRoleCountDTOv1> {

    @MapperModePointerBinding(modes = TwinClassMode.SpaceRole2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @MapperModePointerBinding(modes = BusinessAccountMode.SpaceRole2BusinessAccountMode.class)
    private final BusinessAccountDTOMapper businessAccountDTOMapper;

    private final SpaceRoleService spaceRoleService;

    @Override
    public void map(CountResult<SpaceRoleEntity, SpaceRoleGroupField> src, SpaceRoleCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setTwinClassId(entity.getTwinClassId())
                .setBusinessAccountId(entity.getBusinessAccountId())
                .setCount(src.getCount());
        if (needLoad(mapperContext, TwinClassMode.SpaceRole2TwinClassMode.HIDE, src, SpaceRoleGroupField.twinClassId)) {
            spaceRoleService.loadTwinClass(entity);
            twinClassRestDTOMapper.convertOrPostpone(entity.getTwinClass(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassMode.SpaceRole2TwinClassMode.SHORT)));
        }
        if (needLoad(mapperContext, BusinessAccountMode.SpaceRole2BusinessAccountMode.HIDE, src, SpaceRoleGroupField.businessAccountId)) {
            spaceRoleService.loadBusinessAccount(entity);
            businessAccountDTOMapper.convertOrPostpone(entity.getBusinessAccount(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(BusinessAccountMode.SpaceRole2BusinessAccountMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<SpaceRoleEntity, SpaceRoleGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        if (srcCollection.isEmpty()) {
            return;
        }
        var entityCollection = srcCollection.stream().map(CountResult::getEntity).filter(Objects::nonNull).toList();
        if (entityCollection.isEmpty()) {
            return;
        }
        var sample = srcCollection.iterator().next();
        if (needLoad(mapperContext, TwinClassMode.SpaceRole2TwinClassMode.HIDE, sample, SpaceRoleGroupField.twinClassId)) {
            spaceRoleService.loadTwinClass(entityCollection);
        }
        if (needLoad(mapperContext, BusinessAccountMode.SpaceRole2BusinessAccountMode.HIDE, sample, SpaceRoleGroupField.businessAccountId)) {
            spaceRoleService.loadBusinessAccount(entityCollection);
        }
    }
}
