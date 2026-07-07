package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldDescriptorDTO;
import org.twins.core.dto.rest.twinclass.TwinClassFieldRuleDTOv1;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldrule.fieldoverwriter.FieldParamOverwriter;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassFieldConditionMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassFieldMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassFieldRuleMode;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclass.TwinClassFieldRuleService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinClassFieldRuleMode.class)
public class TwinClassFieldRuleRestDTOMapper extends RestSimpleDTOMapper<TwinClassFieldRuleEntity, TwinClassFieldRuleDTOv1> {

    @MapperModePointerBinding(modes = TwinClassFieldConditionMode.TwinRule2TwinClassFieldConditionMode.class)
    private final TwinClassFieldConditionRestDTOMapper twinClassFieldConditionRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassFieldMode.TwinRule2TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    @MapperModePointerBinding(modes = FeaturerMode.TwinClassFieldRule2FeaturerMode.class)
    private final FeaturerRestDTOMapper featurerRestDTOMapper;

    private final TwinClassFieldDescriptorRestDTOMapper twinClassFieldDescriptorRestDTOMapper;

    private final TwinClassFieldRuleService twinClassFieldRuleService;

    private final FeaturerService featurerService;

    private final PermissionService permissionService;

    @Override
    public void map(TwinClassFieldRuleEntity src, TwinClassFieldRuleDTOv1 dst, MapperContext mapperContext) throws Exception {
        if (src == null || dst == null)
            return;
        switch (mapperContext.getModeOrUse(TwinClassFieldRuleMode.DETAILED)) {
            case MANAGED:
                if (!permissionService.currentUserHasPermission(Permissions.TWIN_CLASS_FIELD_MANAGE))
                    throw new ServiceException(ErrorCodeTwins.SHOW_MODE_ACCESS_DENIED, "Show Mode[" + TwinClassFieldMode.MANAGED + "] is not allowed for current user");
                dst
                        .setFieldOverwriterFeaturerId(src.getFieldOverwriterFeaturerId())
                        .setFieldOverwriterParams(src.getFieldOverwriterParams());
                if (mapperContext.hasModeButNot(FeaturerMode.TwinClassFieldRule2FeaturerMode.HIDE)) {
                    dst.setFieldOverwriterFeaturerId(src.getFieldOverwriterFeaturerId());
                    featurerRestDTOMapper.postpone(src.getFieldOverwriterFeaturerId(), mapperContext.forkOnPoint(FeaturerMode.TwinClassFieldRule2FeaturerMode.SHORT));
                }
            default:
                dst
                        .setId(src.getId())
                        .setOverwrittenValue(src.getOverwrittenValue())
                        .setOverwrittenRequired(src.getOverwrittenRequired())
                        .setRulePriority(src.getRulePriority());
        }

        if (src.getFieldOverwriterFeaturerId() != null) {
            FieldParamOverwriter overwriter = featurerService.getFeaturer(src.getFieldOverwriterFeaturerId(), FieldParamOverwriter.class);
            FieldDescriptor descriptor = overwriter.getFieldOverwriterDescriptor(src);
            TwinClassFieldDescriptorDTO dto = twinClassFieldDescriptorRestDTOMapper.convert(descriptor, mapperContext);
            dst.setOverwrittenDescriptor(dto);
        }
        if (mapperContext.hasModeButNot(TwinClassFieldConditionMode.TwinRule2TwinClassFieldConditionMode.HIDE)) {
            twinClassFieldRuleService.loadConditions(src);
            dst.setConditions(twinClassFieldConditionRestDTOMapper.convertCollection(src.getConditionKit(), mapperContext.forkOnPoint(TwinClassFieldConditionMode.TwinRule2TwinClassFieldConditionMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(TwinClassFieldMode.TwinRule2TwinClassFieldMode.HIDE)) {
            twinClassFieldRuleService.loadRuleFields(src);
            dst.setFields(twinClassFieldRestDTOMapper.convertCollection(src.getFieldKit(), mapperContext.forkOnPoint(TwinClassFieldMode.TwinRule2TwinClassFieldMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinClassFieldRuleEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(TwinClassFieldConditionMode.TwinRule2TwinClassFieldConditionMode.HIDE))
            twinClassFieldRuleService.loadConditions(srcCollection);
        if (mapperContext.hasModeButNot(TwinClassFieldMode.TwinRule2TwinClassFieldMode.HIDE))
            twinClassFieldRuleService.loadRuleFields(srcCollection);
    }

    @Override
    public String getObjectCacheId(TwinClassFieldRuleEntity src) {
        return src.getId() != null ? src.getId().toString() : null;
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinClassFieldRuleMode.HIDE);
    }
}
