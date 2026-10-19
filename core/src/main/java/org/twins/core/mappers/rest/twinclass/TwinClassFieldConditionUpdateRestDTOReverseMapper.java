package org.twins.core.mappers.rest.twinclass;

import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldConditionUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TwinClassFieldConditionUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFieldConditionUpdateDTOv1, TwinClassFieldConditionEntity> {

    @Override
    public void map(TwinClassFieldConditionUpdateDTOv1 src, TwinClassFieldConditionEntity dst, MapperContext mapperContext) {
        if (src == null || dst == null)
            return;
        dst
                .setId(src.getId())
                .setBaseTwinClassFieldId(src.getBaseTwinClassFieldId())
                .setConditionOrder(src.getConditionOrder())
                .setParentTwinClassFieldConditionId(src.getParentTwinClassFieldConditionId())
                .setLogicOperatorId(src.getLogicOperator())
                .setConditionEvaluatorFeaturerId(src.getConditionEvaluatorFeaturerId())
                .setConditionEvaluatorParams(src.getConditionEvaluatorParams());
    }
}
