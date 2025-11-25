package org.twins.core.mappers.rest.twinclass;

import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.domain.twinclass.TwinClassFieldConditionTree;
import org.twins.core.dto.rest.twinclass.TwinClassFieldConditionTreeCreateDTOv1;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.List;
import java.util.ArrayList;

import static org.twins.core.service.twinclass.TwinClassFieldConditionService.MAX_RECURSION_DEPTH;

@Component
public class TwinClassFieldConditionTreeRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFieldConditionTreeCreateDTOv1, TwinClassFieldConditionTree> {

    @Override
    public void map(TwinClassFieldConditionTreeCreateDTOv1 src, TwinClassFieldConditionTree dst, MapperContext mapperContext) throws Exception {
        mapWithDepth(src, dst, mapperContext, 0);
    }

    private void mapWithDepth(TwinClassFieldConditionTreeCreateDTOv1 src, TwinClassFieldConditionTree dst, MapperContext mapperContext, int currentDepth) throws Exception {
        if (currentDepth >= MAX_RECURSION_DEPTH)
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_CONDITION_DEPTH_EXCEEDED);

        dst
                .setBaseTwinClassFieldId(src.getBaseTwinClassFieldId())
                .setConditionOrder(src.getConditionOrder())
                .setConditionEvaluatorFeaturerId(src.getConditionEvaluatorFeaturerId())
                .setConditionEvaluatorParams(src.getConditionEvaluatorParams())
                .setLogicOperator(src.getLogicOperator());

        if (src.getChildConditions() != null && !src.getChildConditions().isEmpty()) {
            List<TwinClassFieldConditionTree> childTrees = new ArrayList<>();

            for (TwinClassFieldConditionTreeCreateDTOv1 childDto : src.getChildConditions()) {
                TwinClassFieldConditionTree childTree = new TwinClassFieldConditionTree();
                mapWithDepth(childDto, childTree, mapperContext, currentDepth + 1);
                childTrees.add(childTree);
            }

            dst.setChildConditions(childTrees);
        } else {
            dst.setChildConditions(null);
        }
    }
}
