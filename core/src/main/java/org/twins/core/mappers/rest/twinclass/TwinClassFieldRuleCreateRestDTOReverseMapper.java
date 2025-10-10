package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldRuleCreateDTOv1;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TwinClassFieldRuleCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFieldRuleCreateDTOv1, TwinClassFieldRuleEntity> {
    private static int FIELD_OVERWRITER_STUB_ID = FeaturerTwins.ID_4601; // "no overwriter" stub featurer
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
        } else {
            dst.setFieldOverwriterFeaturerId(FIELD_OVERWRITER_STUB_ID);
        }

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
