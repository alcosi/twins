package org.twins.core.mappers.rest.space;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.space.SpaceRoleEntity;
import org.twins.core.dto.rest.space.SpaceRoleDTOv1;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.businessaccount.BusinessAccountDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.BusinessAccountMode;
import org.twins.core.mappers.rest.mappercontext.modes.SpaceRoleMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.service.i18n.I18nService;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = SpaceRoleMode.class)
public class SpaceRoleDTOMapper extends RestSimpleDTOMapper<SpaceRoleEntity, SpaceRoleDTOv1> {

    private final I18nService i18nService;

    @MapperModePointerBinding(modes = TwinClassMode.SpaceRole2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @MapperModePointerBinding(modes = BusinessAccountMode.SpaceRole2BusinessAccountMode.class)
    private final BusinessAccountDTOMapper businessAccountDTOMapper;

    @Override
    public void map(SpaceRoleEntity src, SpaceRoleDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(SpaceRoleMode.DETAILED)) {
            case DETAILED ->
                dst
                        .setId(src.getId())
                        .setKey(src.getKey())
                        .setName(I18nCacheHolder.addId(src.getNameI18NId()))
                        .setDescription(I18nCacheHolder.addId(src.getDescriptionI18NId()))
                        .setTwinClassId(src.getTwinClassId())
                        .setBusinessAccountId(src.getBusinessAccountId());
            case SHORT ->
                dst
                        .setId(src.getId())
                        .setKey(src.getKey());
        }

        if (mapperContext.hasModeButNot(TwinClassMode.SpaceRole2TwinClassMode.HIDE)) {
            dst.setTwinClassId(src.getTwinClassId());
            twinClassRestDTOMapper.postpone(src.getTwinClass(), mapperContext.forkOnPoint(TwinClassMode.SpaceRole2TwinClassMode.SHORT));
        }
        if (mapperContext.hasModeButNot(BusinessAccountMode.SpaceRole2BusinessAccountMode.HIDE)) {
            dst.setBusinessAccountId(src.getBusinessAccountId());
            businessAccountDTOMapper.postpone(src.getBusinessAccount(), mapperContext.forkOnPoint(BusinessAccountMode.SpaceRole2BusinessAccountMode.SHORT));
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(SpaceRoleMode.HIDE);
    }

    @Override
    public String getObjectCacheId(SpaceRoleEntity src) {
        return src.getId().toString();
    }
}
