package org.twins.core.mappers.rest.twinclass;

import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldConditionDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.HashMap;

@Component
public class TwinClassFieldConditionRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFieldConditionDTOv1, TwinClassFieldConditionEntity> {
    @Override
    public void map(TwinClassFieldConditionDTOv1 src, TwinClassFieldConditionEntity dst, MapperContext mapperContext) {
        if (src == null || dst == null)
            return;
        dst
                .setId(src.getId())
                .setRuleId(src.getRuleId())
                .setBaseTwinClassFieldId(src.getBaseTwinClassFieldId())
                .setConditionOrder(src.getConditionOrder())
                .setGroupNo(src.getGroupNo());
        if (src.getConditionOperator() != null)
            dst.setConditionOperator(src.getConditionOperator());
        dst.setCmpValue(src.getCmpValue())
                .setCmpParams((HashMap<String, String>) src.getCmpParams());
        if (src.getEvaluatedElement() != null)
            dst.setEvaluatedElement(src.getEvaluatedElement());
        dst.setEvaluatedParamKey(src.getEvaluatedParamKey());
    }
}
