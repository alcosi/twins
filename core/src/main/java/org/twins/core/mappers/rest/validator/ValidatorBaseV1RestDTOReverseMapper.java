package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.validator.TwinflowTransitionValidatorRuleEntity;
import org.twins.core.dto.rest.validator.TwinValidatorBaseDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class ValidatorBaseV1RestDTOReverseMapper extends RestSimpleDTOMapper<TwinValidatorBaseDTOv1, TwinflowTransitionValidatorRuleEntity> {

    @Override
    public void map(TwinValidatorBaseDTOv1 src, TwinflowTransitionValidatorRuleEntity dst, MapperContext mapperContext) throws Exception {
        //TODO support new logic with sets for CUD validator
                dst
                        .setOrder(src.getOrder());
//                        .setTwinValidatorFeaturerId(src.getValidatorFeaturerId())
//                        .setTwinValidatorParams(src.getValidatorParams())
//                        .setInvert(src.getInvert())
//                        .setActive(src.getActive());
    }
}
