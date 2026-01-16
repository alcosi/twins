package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldDTOv1;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.permission.PermissionRestDTOMapper;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclass.TwinClassFieldRuleMapService;

import java.util.Collection;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinClassFieldMode.class)
public class TwinClassFieldRestDTOMapper extends RestSimpleDTOMapper<TwinClassFieldEntity, TwinClassFieldDTOv1> {

    private final TwinClassFieldDescriptorRestDTOMapper twinClassFieldDescriptorRestDTOMapper;

    @Lazy
    @MapperModePointerBinding(modes = TwinClassMode.TwinClassField2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @MapperModePointerBinding(modes = PermissionMode.TwinClassField2PermissionMode.class)
    private final PermissionRestDTOMapper permissionRestDTOMapper;

    @MapperModePointerBinding(modes = FeaturerMode.TwinClassField2FeaturerMode.class)
    private final FeaturerRestDTOMapper featurerRestDTOMapper;

    @Lazy
    @MapperModePointerBinding(modes = TwinClassFieldRuleMode.TwinClassField2TwinClassFieldRuleMode.class)
    private final TwinClassFieldRuleRestDTOMapper twinClassFieldRuleRestDTOMapper;

    private final I18nService i18nService;
    private final FeaturerService featurerService;
    private final PermissionService permissionService;
    private final TwinClassFieldRuleMapService twinClassFieldRuleMapService;


    @Override
    public void map(TwinClassFieldEntity src, TwinClassFieldDTOv1 dst, MapperContext mapperContext) throws Exception {
        FieldTyper fieldTyper;
        FieldDescriptor fieldDescriptor;
        switch (mapperContext.getModeOrUse(TwinClassFieldMode.DETAILED)) {
            case MANAGED:
                if (!permissionService.currentUserHasPermission(Permissions.TWIN_CLASS_MANAGE))
                    throw new ServiceException(ErrorCodeTwins.SHOW_MODE_ACCESS_DENIED, "Show Mode[" + TwinClassFieldMode.MANAGED + "] is not allowed for current user");
                fieldTyper = featurerService.getFeaturer(src.getFieldTyperFeaturerId(), FieldTyper.class);
                fieldDescriptor = fieldTyper.getFieldDescriptor(src);
                dst
                        .setId(src.getId())
                        .setKey(src.getKey())
                        .setName(I18nCacheHolder.addId(src.getNameI18nId()))
                        .setRequired(src.getRequired())
                        .setDescription(I18nCacheHolder.addId(src.getDescriptionI18nId()))
                        .setTwinClassId(src.getTwinClassId())
                        .setNameI18nId(src.getNameI18nId())
                        .setDescriptionI18nId(src.getDescriptionI18nId())
                        .setFieldTyperFeaturerId(src.getFieldTyperFeaturerId())
                        .setFieldTyperParams(src.getFieldTyperParams())
                        .setTwinSorterFeaturerId(src.getTwinSorterFeaturerId())
                        .setTwinSorterParams(src.getFieldTyperParams())
                        .setViewPermissionId(src.getViewPermissionId())
                        .setEditPermissionId(src.getEditPermissionId())
                        .setDescriptor(twinClassFieldDescriptorRestDTOMapper.convert(fieldDescriptor, mapperContext))
                        .setFeValidationErrorI18nId(src.getFeValidationErrorI18nId())
                        .setFeValidationError(I18nCacheHolder.addId(src.getFeValidationErrorI18nId()))
                        .setBeValidationErrorI18nId(src.getBeValidationErrorI18nId())
                        .setBeValidationError(I18nCacheHolder.addId(src.getBeValidationErrorI18nId()))
                        .setExternalId(src.getExternalId())
                        .setSystem(src.getSystem())
                        .setDependent(src.getDependentField())
                        .setHasDependentFields(src.getHasDependentFields())
                        .setExternalProperties(src.getExternalProperties())
                        .setOrder(src.getOrder())
                        .setHasProjectedFields(src.getHasProjectedFields())
                        .setProjectionField(src.getProjectionField());
                if (mapperContext.hasModeButNot(FeaturerMode.TwinClassField2FeaturerMode.HIDE)) {
                    dst.setFieldTyperFeaturerId(src.getFieldTyperFeaturerId());
                    dst.setTwinSorterFeaturerId(src.getTwinSorterFeaturerId());
                    featurerRestDTOMapper.postpone(featurerService.getFeaturerEntity(src.getFieldTyperFeaturerId()), mapperContext.forkOnPoint(FeaturerMode.TwinClassField2FeaturerMode.SHORT));
                    featurerRestDTOMapper.postpone(featurerService.getFeaturerEntity(src.getTwinSorterFeaturerId()), mapperContext.forkOnPoint(FeaturerMode.TwinClassField2FeaturerMode.SHORT));
                }
                if (mapperContext.hasModeButNot(PermissionMode.TwinClassField2PermissionMode.HIDE)) {
                    dst
                            .setViewPermissionId(src.getViewPermissionId())
                            .setEditPermissionId(src.getEditPermissionId());
                    permissionRestDTOMapper.postpone(src.getViewPermission(), mapperContext.forkOnPoint(PermissionMode.TwinClassField2PermissionMode.SHORT));
                    permissionRestDTOMapper.postpone(src.getEditPermission(), mapperContext.forkOnPoint(PermissionMode.TwinClassField2PermissionMode.SHORT));
                }
                break;
            case DETAILED:
                fieldTyper = featurerService.getFeaturer(src.getFieldTyperFeaturerId(), FieldTyper.class);
                fieldDescriptor = fieldTyper.getFieldDescriptor(src);
                dst
                        .setId(src.getId())
                        .setKey(src.getKey())
                        .setName(I18nCacheHolder.addId(src.getNameI18nId()))
                        .setRequired(src.getRequired())
                        .setDescription(I18nCacheHolder.addId(src.getDescriptionI18nId()))
                        .setDescriptor(twinClassFieldDescriptorRestDTOMapper.convert(fieldDescriptor, mapperContext))
                        .setFeValidationError(I18nCacheHolder.addId(src.getFeValidationErrorI18nId()))
                        .setExternalId(src.getExternalId())
                        .setSystem(src.getSystem())
                        .setDependent(src.getDependentField())
                        .setHasDependentFields(src.getHasDependentFields())
                        .setExternalProperties(src.getExternalProperties())
                        .setOrder(src.getOrder())
                        .setHasProjectedFields(src.getHasProjectedFields())
                        .setProjectionField(src.getProjectionField());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey())
                        .setName(I18nCacheHolder.addId(src.getNameI18nId()));
                break;
        }
        if (mapperContext.hasModeButNot(TwinClassMode.TwinClassField2TwinClassMode.HIDE)) {
            dst.setTwinClassId(src.getTwinClassId());
            twinClassRestDTOMapper.postpone(src.getTwinClass(), mapperContext.forkOnPoint(TwinClassMode.TwinClassField2TwinClassMode.SHORT));
        }
        if (mapperContext.hasModeButNot(TwinClassFieldRuleMode.TwinClassField2TwinClassFieldRuleMode.HIDE)) {
            twinClassFieldRuleMapService.loadRules(src);
            dst.setRuleIds(src.getRuleKit().getIdSet());
            twinClassFieldRuleRestDTOMapper.postpone(src.getRuleKit(), mapperContext.forkOnPoint(TwinClassFieldRuleMode.TwinClassField2TwinClassFieldRuleMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinClassFieldEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(TwinClassFieldRuleMode.TwinClassField2TwinClassFieldRuleMode.HIDE)) {
            //preload rules for all fields in srcCollection to avoid n+1 problem
            twinClassFieldRuleMapService.loadRules(srcCollection);
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
