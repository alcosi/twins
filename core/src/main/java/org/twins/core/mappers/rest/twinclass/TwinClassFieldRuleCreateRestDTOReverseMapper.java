package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.domain.twinclass.TwinClassFieldRuleSave;
import org.twins.core.dto.rest.twinclass.TwinClassFieldRuleCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinClassFieldRuleCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFieldRuleCreateDTOv1, TwinClassFieldRuleSave> {
    private final TwinClassFieldConditionTreeRestDTOReverseMapper twinClassFieldConditionTreeRestDTOReverseMapper;

    @Override
    public void map(TwinClassFieldRuleCreateDTOv1 src, TwinClassFieldRuleSave dst, MapperContext mapperContext) throws Exception {
        if (src == null || dst == null)
            return;
        // map simple scalar fields
        dst.setTwinClassFieldRule(new TwinClassFieldRuleEntity()
                .setOverwrittenValue(src.getOverwrittenValue())
                .setOverwrittenRequired(src.getOverwrittenRequired())
                .setRulePriority(src.getRulePriority()));
        dst.setTwinClassFieldIds(src.getDependentTwinClassFieldIds());
        if (src.getFieldParamOverwriterFeaturerId() != null) {
            dst.getTwinClassFieldRule()
                    .setFieldOverwriterFeaturerId(src.getFieldParamOverwriterFeaturerId())
                    .setFieldOverwriterParams(src.getFieldParamOverwriterParams());
        }

        //map conditions (if provided)
        if (CollectionUtils.isNotEmpty(src.getConditions())) {
            dst.setTwinClassFieldConditionTrees(twinClassFieldConditionTreeRestDTOReverseMapper.convertCollection(src.getConditions(), mapperContext));
        }
    }
}
