package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldRuleCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinClassFieldRuleCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFieldRuleCreateDTOv1, TwinClassFieldRuleEntity> {
    private final TwinClassFieldConditionRestDTOReverseMapper twinClassFieldConditionRestDTOReverseMapper;

    @Override
    public void map(TwinClassFieldRuleCreateDTOv1 src, TwinClassFieldRuleEntity dst, MapperContext mapperContext) throws Exception {
        if (src == null || dst == null)
            return;
        // map simple scalar fields
        dst
                .setTwinClassFieldId(src.getDependentTwinClassFieldId())
                .setOverwrittenValue(src.getOverwrittenValue())
                .setOverwrittenRequired(src.getOverwrittenRequired())
                .setRulePriority(src.getRulePriority());
        if (src.getFieldParamOverwriterFeaturerId() != null) {
            dst.setFieldOverwriterFeaturerId(src.getFieldParamOverwriterFeaturerId());
            dst.setFieldOverwriterParams(src.getFieldParamOverwriterParams());
        }

        // map conditions (if provided)
        if (CollectionUtils.isNotEmpty(src.getConditions())) {
            dst.setConditionKit(new Kit<>(twinClassFieldConditionRestDTOReverseMapper.convertCollection(src.getConditions()), TwinClassFieldConditionEntity::getId));
        }
    }
}
