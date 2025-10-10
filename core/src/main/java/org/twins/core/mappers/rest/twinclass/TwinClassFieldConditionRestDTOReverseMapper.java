package org.twins.core.mappers.rest.twinclass;

import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldConditionCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.UUID;

@Component
public class TwinClassFieldConditionRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFieldConditionCreateDTOv1, TwinClassFieldConditionEntity> {
    @Override
    public void map(TwinClassFieldConditionCreateDTOv1 src, TwinClassFieldConditionEntity dst, MapperContext mapperContext) {
        if (src == null || dst == null)
            return;
        dst
                .setId(UUID.randomUUID())
                .setBaseTwinClassFieldId(src.getBaseTwinClassFieldId())
                .setConditionOrder(src.getConditionOrder())
                .setGroupNo(src.getGroupNo())
                .setConditionEvaluatorFeaturerId(src.getConditionEvaluatorFeaturerId())
                .setConditionEvaluatorParams(src.getConditionEvaluatorParams());
    }
}
