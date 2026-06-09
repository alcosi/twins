package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.twinclass.TwinClassFieldCountDTOv1;
import org.twins.core.enums.sort.TwinClassFieldGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerMode;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.permission.PermissionRestDTOMapper;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class TwinClassFieldCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<TwinClassFieldEntity, TwinClassFieldGroupField>, TwinClassFieldCountDTOv1> {
    @MapperModePointerBinding(modes = TwinClassMode.TwinClassField2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    private final TwinClassFieldService twinClassFieldService;

    @MapperModePointerBinding(modes = PermissionMode.TwinClass2PermissionMode.class)
    private final PermissionRestDTOMapper permissionRestDTOMapper;

    @MapperModePointerBinding(modes = FeaturerMode.TwinClassField2FeaturerMode.class)
    private final FeaturerRestDTOMapper featurerRestDTOMapper;

    @Override
    public void map(CountResult<TwinClassFieldEntity, TwinClassFieldGroupField> src, TwinClassFieldCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setRequired(entity.getRequired())
                .setInheritable(entity.getInheritable())
                .setSystem(entity.getSystem())
                .setDependentField(entity.getDependentField())
                .setHasDependentFields(entity.getHasDependentFields())
                .setProjectionField(entity.getProjectionField())
                .setHasProjectedFields(entity.getHasProjectedFields())
                .setTwinClassId(entity.getTwinClassId())
                .setFieldTyperFeaturerId(entity.getFieldTyperFeaturerId())
                .setTwinSorterFeaturerId(entity.getTwinSorterFeaturerId())
                .setFieldInitializerFeaturerId(entity.getFieldInitializerFeaturerId())
                .setViewPermissionId(entity.getViewPermissionId())
                .setEditPermissionId(entity.getEditPermissionId())
                .setCount(src.getCount());
        if (needLoad(mapperContext, TwinClassMode.TwinClassField2TwinClassMode.HIDE, src, TwinClassFieldGroupField.twinClassId)) {
            twinClassFieldService.loadTwinClass(entity);
            twinClassRestDTOMapper.postpone(entity.getTwinClass(), mapperContext.forkOnPoint(TwinClassMode.TwinClassField2TwinClassMode.SHORT));
        }
        if (needLoad(mapperContext, PermissionMode.TwinClassField2PermissionMode.HIDE, src, TwinClassFieldGroupField.viewPermissionId, TwinClassFieldGroupField.editPermissionId)) {
            twinClassFieldService.loadPermissions(entity);
            permissionRestDTOMapper.postpone(entity.getViewPermission(), mapperContext.forkOnPoint(PermissionMode.TwinClass2PermissionMode.SHORT));
            permissionRestDTOMapper.postpone(entity.getEditPermission(), mapperContext.forkOnPoint(PermissionMode.TwinClass2PermissionMode.SHORT));
        }
        if (needLoad(mapperContext, FeaturerMode.TwinClassField2FeaturerMode.HIDE, src, TwinClassFieldGroupField.fieldInitializerFeaturerId, TwinClassFieldGroupField.fieldTyperFeaturerId, TwinClassFieldGroupField.twinSorterFeaturerId)) {
            featurerRestDTOMapper.postpone(entity.getFieldInitializerFeaturerId(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FeaturerMode.TwinClassField2FeaturerMode.SHORT)));
            featurerRestDTOMapper.postpone(entity.getFieldTyperFeaturerId(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FeaturerMode.TwinClassField2FeaturerMode.SHORT)));
            featurerRestDTOMapper.postpone(entity.getTwinSorterFeaturerId(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FeaturerMode.TwinClassField2FeaturerMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<TwinClassFieldEntity, TwinClassFieldGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        var entities = srcCollection.stream().map(CountResult::getEntity).toList();
        var someCount = srcCollection.iterator().next();
        if (needLoad(mapperContext, TwinClassMode.TwinClassField2TwinClassMode.HIDE, someCount, TwinClassFieldGroupField.twinClassId)) {
            twinClassFieldService.loadTwinClass(entities);
        }
        if (needLoad(mapperContext, PermissionMode.TwinClassField2PermissionMode.HIDE, someCount, TwinClassFieldGroupField.viewPermissionId, TwinClassFieldGroupField.editPermissionId)) {
            twinClassFieldService.loadPermissions(entities);
        }
    }
}
