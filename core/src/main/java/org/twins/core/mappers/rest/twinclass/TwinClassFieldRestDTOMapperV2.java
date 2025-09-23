package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerMode;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassFieldMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.permission.PermissionRestDTOMapper;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinClassFieldMode.class)
public class TwinClassFieldRestDTOMapperV2 extends RestSimpleDTOMapper<TwinClassFieldEntity, TwinClassFieldDTOv2> {
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassMode.TwinClassField2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @MapperModePointerBinding(modes = PermissionMode.TwinClassField2PermissionMode.class)
    private final PermissionRestDTOMapper permissionRestDTOMapper;

    @MapperModePointerBinding(modes = FeaturerMode.TwinClassField2FeaturerMode.class)
    private final FeaturerRestDTOMapper featurerRestDTOMapper;

    @Override
    public void map(TwinClassFieldEntity src, TwinClassFieldDTOv2 dst, MapperContext mapperContext) throws Exception {
        twinClassFieldRestDTOMapper.map(src, dst, mapperContext);
        if (!mapperContext.hasMode(TwinClassFieldMode.MANAGED))
            return;
        // Resolution checking is in the first version of the TwinClassFieldRestDTOMapper
        if (mapperContext.hasModeButNot(TwinClassMode.TwinClassField2TwinClassMode.HIDE))
            dst
                    .setTwinClass(twinClassRestDTOMapper.convertOrPostpone(src.getTwinClass(), mapperContext.forkOnPoint(TwinClassMode.TwinClassField2TwinClassMode.SHORT)))
                    .setTwinClassId(src.getTwinClassId());
        if (mapperContext.hasModeButNot(FeaturerMode.TwinClassField2FeaturerMode.HIDE))
            dst
                    .setFieldTyperFeaturer(featurerRestDTOMapper.convertOrPostpone(src.getFieldTyperFeaturer(), mapperContext.forkOnPoint(FeaturerMode.TwinClassField2FeaturerMode.SHORT)))
                    .setTwinSorterFeaturer(featurerRestDTOMapper.convertOrPostpone(src.getTwinSorterFeaturer(), mapperContext.forkOnPoint(FeaturerMode.TwinClassField2FeaturerMode.SHORT)))
                    .setFieldTyperFeaturerId(src.getFieldTyperFeaturerId())
                    .setTwinSorterFeaturerId(src.getTwinSorterFeaturerId());
        if (mapperContext.hasModeButNot(PermissionMode.TwinClassField2PermissionMode.HIDE)) {
            dst
                    .setViewPermission(permissionRestDTOMapper.convertOrPostpone(src.getViewPermission(), mapperContext.forkOnPoint(PermissionMode.TwinClassField2PermissionMode.SHORT)))
                    .setViewPermissionId(src.getViewPermissionId());
            dst
                    .setEditPermission(permissionRestDTOMapper.convertOrPostpone(src.getEditPermission(), mapperContext.forkOnPoint(PermissionMode.TwinClassField2PermissionMode.SHORT)))
                    .setEditPermissionId(src.getEditPermissionId());
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinClassFieldMode.HIDE);
    }

    @Override
    public String getObjectCacheId(TwinClassFieldEntity src) {
        return src.getId().toString();
    }
}
