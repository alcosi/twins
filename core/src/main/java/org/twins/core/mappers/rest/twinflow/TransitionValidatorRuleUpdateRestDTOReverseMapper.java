package org.twins.core.mappers.rest.twinflow;

import org.springframework.stereotype.Component;
import org.twins.core.dao.validator.TwinflowTransitionValidatorRuleEntity;
import org.twins.core.dto.rest.validator.cud.TransitionValidatorRuleUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TransitionValidatorRuleUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<TransitionValidatorRuleUpdateDTOv1, TwinflowTransitionValidatorRuleEntity> {

    //todo think about cud logic

    @Override
    public void map(TransitionValidatorRuleUpdateDTOv1 src, TwinflowTransitionValidatorRuleEntity dst, MapperContext mapperContext) throws Exception {
//        twinValidatorBaseRestDTOReverseMapper.map(src, dst, mapperContext);
//        dst.setId(src.getId());
    }
}
