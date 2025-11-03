package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.featurer.FeaturerService;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldDescriptorDTO;
import org.twins.core.dto.rest.twinclass.TwinClassFieldRuleDTOv1;
import org.twins.core.featurer.fieldrule.fieldoverwriter.FieldParamOverwriter;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassFieldConditionMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassFieldMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassFieldRuleMode;
import org.twins.core.service.twinclass.TwinClassFieldConditionService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinClassFieldRuleMode.class)
public class TwinClassFieldRuleRestDTOMapper extends RestSimpleDTOMapper<TwinClassFieldRuleEntity, TwinClassFieldRuleDTOv1> {

    @MapperModePointerBinding(modes = TwinClassFieldConditionMode.TwinRule2TwinClassFieldConditionMode.class)
    private final TwinClassFieldConditionRestDTOMapper twinClassFieldConditionRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassFieldMode.TwinRule2TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    private final TwinClassFieldDescriptorRestDTOMapper twinClassFieldDescriptorRestDTOMapper;

    private final TwinClassFieldConditionService twinClassFieldConditionService;

    private final TwinClassFieldService twinClassFieldService;

    private final FeaturerService featurerService;

    @Override
    public void map(TwinClassFieldRuleEntity src, TwinClassFieldRuleDTOv1 dst, MapperContext mapperContext) throws Exception {
        if (src == null || dst == null)
            return;
        // map scalar fields
        dst
                .setId(src.getId())
                .setOverwrittenValue(src.getOverwrittenValue())
                .setOverwrittenRequired(src.getOverwrittenRequired())
                .setRulePriority(src.getRulePriority());
        if (src.getFieldOverwriterFeaturerId() != null) {
            FieldParamOverwriter overwriter = featurerService.getFeaturer(src.getFieldOverwriterFeaturerId(), FieldParamOverwriter.class);
            FieldDescriptor descriptor = overwriter.getFieldOverwriterDescriptor(src);
            TwinClassFieldDescriptorDTO dto = twinClassFieldDescriptorRestDTOMapper.convert(descriptor, mapperContext);
            dst.setOverwrittenDescriptor(dto);
        }
        if (mapperContext.hasModeButNot(TwinClassFieldConditionMode.TwinRule2TwinClassFieldConditionMode.HIDE)) {
            twinClassFieldConditionService.loadConditions(src);
            dst.setConditions(twinClassFieldConditionRestDTOMapper.convertCollection(src.getConditionKit(), mapperContext.forkOnPoint(TwinClassFieldConditionMode.TwinRule2TwinClassFieldConditionMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(TwinClassFieldMode.TwinRule2TwinClassFieldMode.HIDE)) {
            twinClassFieldService.loadRuleFields(src);
            dst.setFields(twinClassFieldRestDTOMapper.convertCollection(src.getFieldKit(), mapperContext.forkOnPoint(TwinClassFieldMode.TwinRule2TwinClassFieldMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinClassFieldRuleEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(TwinClassFieldConditionMode.TwinRule2TwinClassFieldConditionMode.HIDE))
            twinClassFieldConditionService.loadConditions(srcCollection);
        if (mapperContext.hasModeButNot(TwinClassFieldMode.TwinRule2TwinClassFieldMode.HIDE))
            twinClassFieldService.loadRuleFields(srcCollection);
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
