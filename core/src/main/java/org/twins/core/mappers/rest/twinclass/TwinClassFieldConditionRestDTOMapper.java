package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldConditionDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldConditionDescriptorDTO;
import org.twins.core.featurer.fieldrule.conditionevaluator.ConditionEvaluator;
import org.twins.core.featurer.fieldrule.conditionevaluator.conditiondescriptor.ConditionDescriptor;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassFieldConditionMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassFieldMode;
import org.twins.core.service.twinclass.TwinClassFieldConditionService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {TwinClassFieldConditionMode.class})
public class TwinClassFieldConditionRestDTOMapper extends RestSimpleDTOMapper<TwinClassFieldConditionEntity, TwinClassFieldConditionDTOv1> {

    private final FeaturerService featurerService;
    private final TwinClassFieldConditionDescriptorRestDTOMapper conditionDescriptorMapper;
    @Lazy
    @MapperModePointerBinding(modes = TwinClassFieldMode.TwinClassFieldCondition2TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;
    private final TwinClassFieldConditionService twinClassFieldConditionService;

    @Override
    public void map(TwinClassFieldConditionEntity src, TwinClassFieldConditionDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinClassFieldConditionMode.DETAILED)) {
            case HIDE -> {
                // do nothing – object is hidden
            }
            case SHORT -> {
                // minimal representation
                dst
                        .setId(src.getId())
                        .setRuleId(src.getTwinClassFieldRuleId());
            }
            case DETAILED -> {
                // full mapping
                dst
                        .setId(src.getId())
                        .setRuleId(src.getTwinClassFieldRuleId())
                        .setBaseTwinClassFieldId(src.getBaseTwinClassFieldId())
                        .setConditionOrder(src.getConditionOrder())
                        .setParentTwinClassFieldConditionId(src.getParentTwinClassFieldConditionId())
                        .setLogicOperatorId(src.getLogicOperatorId());
                if (src.getConditionEvaluatorFeaturerId() != null) {
                    ConditionEvaluator<?> evaluator = featurerService.getFeaturer(src.getConditionEvaluatorFeaturerId(), ConditionEvaluator.class);
                    ConditionDescriptor descriptor = evaluator.getConditionDescriptor(src);
                    TwinClassFieldConditionDescriptorDTO dto = conditionDescriptorMapper.convert(descriptor, mapperContext);
                    dst.setConditionDescriptor(dto);
                }
            }
        }
        if (mapperContext.hasModeButNot(TwinClassFieldMode.TwinClassFieldCondition2TwinClassFieldMode.HIDE)) {
            twinClassFieldConditionService.loadBaseTwinClassField(src);
            dst.setBaseTwinClassFieldId(src.getBaseTwinClassFieldId());
            twinClassFieldRestDTOMapper.postpone(src.getBaseTwinClassField(), mapperContext.forkOnPoint(TwinClassFieldMode.TwinClassFieldCondition2TwinClassFieldMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinClassFieldConditionEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(TwinClassFieldMode.TwinClassFieldCondition2TwinClassFieldMode.HIDE)) {
            twinClassFieldConditionService.loadBaseTwinClassFields(srcCollection);
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