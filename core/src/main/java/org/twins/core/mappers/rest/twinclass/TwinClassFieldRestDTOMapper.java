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
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.permission.PermissionRestDTOMapper;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclass.TwinClassFieldRuleService;


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

    @MapperModePointerBinding(modes = TwinClassDependentFieldBundleMode.TwinField2TwinClassDependentFieldBundleMode.class)
    private final
    TwinClassDependentFieldRestDTOMapperV1 twinClassDependentFieldRestDTOMapperV1;

    private final I18nService i18nService;
    private final FeaturerService featurerService;
    private final PermissionService permissionService;
    private final TwinClassFieldRuleService twinClassFieldRuleService;


    //todo - map rules

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
                        .setName(i18nService.translateToLocale(src.getNameI18nId()))
                        .setRequired(src.getRequired())
                        .setDescription(src.getDescriptionI18nId() != null ? i18nService.translateToLocale(src.getDescriptionI18nId()) : "")
                        .setTwinClassId(src.getTwinClassId())
                        .setNameI18nId(src.getNameI18nId())
                        .setDescriptionI18nId(src.getDescriptionI18nId())
                        .setFieldTyperFeaturerId(src.getFieldTyperFeaturerId())
                        .setTwinSorterFeaturerId(src.getTwinSorterFeaturerId())
                        .setTwinSorterParams(src.getFieldTyperParams())
                        .setViewPermissionId(src.getViewPermissionId())
                        .setEditPermissionId(src.getEditPermissionId())
                        .setDescriptor(twinClassFieldDescriptorRestDTOMapper.convert(fieldDescriptor, mapperContext))
                        .setFeValidationErrorI18nId(src.getFeValidationErrorI18nId())
                        .setFeValidationError(src.getFeValidationErrorI18nId() != null ? i18nService.translateToLocale(src.getFeValidationErrorI18nId()) : "")
                        .setBeValidationErrorI18nId(src.getBeValidationErrorI18nId())
                        .setBeValidationError(src.getBeValidationErrorI18nId() != null ? i18nService.translateToLocale(src.getBeValidationErrorI18nId()) : "")
                        .setExternalId(src.getExternalId())
                        .setExternalProperties(src.getExternalProperties());
                if (mapperContext.hasModeButNot(FeaturerMode.TwinClassField2FeaturerMode.HIDE)) {
                    dst.setFieldTyperFeaturerId(src.getFieldTyperFeaturerId());
                    featurerRestDTOMapper.postpone(featurerService.getFeaturerEntity(src.getFieldTyperFeaturerId()), mapperContext.forkOnPoint(FeaturerMode.TwinClassField2FeaturerMode.SHORT));
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
                        .setName(i18nService.translateToLocale(src.getNameI18nId()))
                        .setRequired(src.getRequired())
                        .setDescription(src.getDescriptionI18nId() != null ? i18nService.translateToLocale(src.getDescriptionI18nId()) : "")
                        .setDescriptor(twinClassFieldDescriptorRestDTOMapper.convert(fieldDescriptor, mapperContext))
                        .setFeValidationError(src.getFeValidationErrorI18nId() != null ? i18nService.translateToLocale(src.getFeValidationErrorI18nId()) : "")
                        .setExternalId(src.getExternalId())
                        .setExternalProperties(src.getExternalProperties());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey());
                break;
        }
        if (mapperContext.hasModeButNot(TwinClassMode.TwinClassField2TwinClassMode.HIDE)) {
            dst.setTwinClassId(src.getTwinClassId());
            twinClassRestDTOMapper.postpone(src.getTwinClass(), mapperContext.forkOnPoint(TwinClassMode.TwinClassField2TwinClassMode.SHORT));
        }
        if (mapperContext.hasModeButNot(TwinClassFieldRuleBundleMode.TwinField2TwinClassFieldRuleBundleMode.HIDE)) {
            dst.setConditionBundles(twinClassDependentFieldRestDTOMapperV1.convertCollection(twinClassFieldRuleService.loadRulesByTwinClassField(src.getId())));
        //todo - do I need to add postpone here?
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
