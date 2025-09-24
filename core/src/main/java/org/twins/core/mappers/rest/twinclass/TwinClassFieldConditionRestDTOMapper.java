package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.featurer.FeaturerService;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldConditionDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldConditionDescriptorDTO;
import org.twins.core.featurer.conditionevaluator.ConditionEvaluator;
import org.twins.core.featurer.conditionevaluator.conditiondescriptor.ConditionDescriptor;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassFieldConditionMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {TwinClassFieldConditionMode.class})
public class TwinClassFieldConditionRestDTOMapper extends RestSimpleDTOMapper<TwinClassFieldConditionEntity, TwinClassFieldConditionDTOv1> {

    private final FeaturerService featurerService;
    private final TwinClassFieldConditionDescriptorRestDTOMapper conditionDescriptorMapper;

    @Override
    public void map(TwinClassFieldConditionEntity src, TwinClassFieldConditionDTOv1 dst, MapperContext mapperContext) throws Exception {
        if (src == null || dst == null)
            return;

        switch (mapperContext.getModeOrUse(TwinClassFieldConditionMode.DETAILED)) {
            case HIDE -> {
                // do nothing – object is hidden
            }
            case SHORT -> {
                // minimal representation
                dst.setId(src.getId())
                   .setRuleId(src.getRuleId());
            }
            case DETAILED -> {
                // full mapping
                dst
                    .setId(src.getId())
                    .setRuleId(src.getRuleId())
                    .setBaseTwinClassFieldId(src.getBaseTwinClassFieldId())
                    .setConditionOrder(src.getConditionOrder())
                    .setGroupNo(src.getGroupNo());
                if (src.getConditionOperator() != null)
                    dst.setConditionOperator(src.getConditionOperator());

                if (src.getConditionEvaluatorFeaturerId() != null) {
                    ConditionEvaluator evaluator = featurerService.getFeaturer(src.getConditionEvaluatorFeaturerId(), ConditionEvaluator.class);
                    ConditionDescriptor descriptor = evaluator.getConditionDescriptor(src);
                    TwinClassFieldConditionDescriptorDTO dto = conditionDescriptorMapper.convert(descriptor, mapperContext);
                    dst.setDescriptor(dto);
                }
            }
        }
    }

    @Override
    public String getObjectCacheId(TwinClassFieldConditionEntity src) {
        return src.getId() != null ? src.getId().toString() : null;
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinClassFieldConditionMode.HIDE);
    }
}