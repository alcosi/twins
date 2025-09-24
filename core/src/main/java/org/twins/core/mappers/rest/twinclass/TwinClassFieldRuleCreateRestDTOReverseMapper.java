package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldRuleCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.stream.Collectors;

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
                .setId(src.getId())
                .setDependentTwinClassFieldId(src.getDependentTwinClassFieldId())
                .setTargetElement(src.getTargetElement())
                .setTargetParamKey(src.getTargetParamKey())
                .setDependentOverwrittenValue(src.getDependentOverwrittenValue())
                .setDependentOverwrittenDatalistId(src.getDependentOverwrittenDatalistId())
                .setRulePriority(src.getRulePriority());

        // map conditions (if provided)
        if (src.getConditions() != null && !src.getConditions().isEmpty()) {
            java.util.Set<org.twins.core.dao.twinclass.TwinClassFieldConditionEntity> conditionEntities = src.getConditions().stream()
                    .map(dto -> {
                        try {
                            return twinClassFieldConditionRestDTOReverseMapper.convert(dto, mapperContext);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toSet());
            dst.setConditions(conditionEntities);
        }
    }
}
