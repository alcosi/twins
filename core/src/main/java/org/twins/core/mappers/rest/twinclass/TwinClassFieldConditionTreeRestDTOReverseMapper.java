package org.twins.core.mappers.rest.twinclass;

import org.springframework.stereotype.Component;
import org.twins.core.domain.twinclass.TwinClassFieldConditionTree;
import org.twins.core.dto.rest.twinclass.TwinClassFieldConditionTreeCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TwinClassFieldConditionTreeRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFieldConditionTreeCreateDTOv1, TwinClassFieldConditionTree> {

    @Override
    public void map(TwinClassFieldConditionTreeCreateDTOv1 src, TwinClassFieldConditionTree dst, MapperContext mapperContext) throws Exception {
        dst
                .setBaseTwinClassFieldId(src.getBaseTwinClassFieldId())
                .setConditionOrder(src.getConditionOrder())
                .setConditionEvaluatorFeaturerId(src.getConditionEvaluatorFeaturerId())
                .setConditionEvaluatorParams(src.getConditionEvaluatorParams())
                .setLogicOperator(src.getLogicOperator())
                .setChildConditions(convertCollection(src.getChildConditions()));
    }
}
