package org.twins.core.mappers.rest.twinflow;

import org.springframework.stereotype.Component;
import org.twins.core.dao.validator.TwinflowTransitionValidatorRuleEntity;
import org.twins.core.dto.rest.validator.cud.TransitionValidatorRuleCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TransitionValidatorRuleCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TransitionValidatorRuleCreateDTOv1, TwinflowTransitionValidatorRuleEntity> {

    //todo think about cud logic

    @Override
    public void map(TransitionValidatorRuleCreateDTOv1 src, TwinflowTransitionValidatorRuleEntity dst, MapperContext mapperContext) throws Exception {
//        twinValidatorBaseRestDTOReverseMapper.map(src, dst, mapperContext);
    }
}
