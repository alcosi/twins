package org.twins.core.mappers.rest.twinclass;

import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldConditionDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassFieldConditionMode;

@Component
@MapperModeBinding(modes = {TwinClassFieldConditionMode.class})
public class TwinClassFieldConditionRestDTOMapper extends RestSimpleDTOMapper<TwinClassFieldConditionEntity, TwinClassFieldConditionDTOv1> {
    @Override
    public void map(TwinClassFieldConditionEntity src, TwinClassFieldConditionDTOv1 dst, MapperContext mapperContext) {
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
        dst
                .setCmpValue(src.getCmpValue())
                .setCmpParams(src.getCmpParams());
        if (src.getEvaluatedElement() != null)
            dst.setEvaluatedElement(src.getEvaluatedElement());
        dst.setEvaluatedParamKey(src.getEvaluatedParamKey());
    }

    @Override
    public String getObjectCacheId(TwinClassFieldConditionEntity src) {
        return src.getId() != null ? src.getId().toString() : null;
    }
}