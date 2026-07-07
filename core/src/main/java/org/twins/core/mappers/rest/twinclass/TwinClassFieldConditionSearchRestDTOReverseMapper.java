package org.twins.core.mappers.rest.twinclass;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinClassFieldConditionSearch;
import org.twins.core.dto.rest.twinclass.TwinClassFieldConditionSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TwinClassFieldConditionSearchRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFieldConditionSearchDTOv1, TwinClassFieldConditionSearch> {

    @Override
    public void map(TwinClassFieldConditionSearchDTOv1 src, TwinClassFieldConditionSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setTwinClassFieldRuleIdList(src.getTwinClassFieldRuleIdList())
                .setTwinClassFieldRuleIdExcludeList(src.getTwinClassFieldRuleIdExcludeList())
                .setBaseTwinClassFieldIdList(src.getBaseTwinClassFieldIdList())
                .setBaseTwinClassFieldIdExcludeList(src.getBaseTwinClassFieldIdExcludeList())
                .setParentTwinClassFieldConditionIdList(src.getParentTwinClassFieldConditionIdList())
                .setParentTwinClassFieldConditionIdExcludeList(src.getParentTwinClassFieldConditionIdExcludeList())
                .setLogicOperatorIdList(src.getLogicOperatorIdList())
                .setLogicOperatorIdExcludeList(src.getLogicOperatorIdExcludeList())
                .setConditionEvaluatorFeaturerIdList(src.getConditionEvaluatorFeaturerIdList())
                .setConditionEvaluatorFeaturerIdExcludeList(src.getConditionEvaluatorFeaturerIdExcludeList());
    }
}
