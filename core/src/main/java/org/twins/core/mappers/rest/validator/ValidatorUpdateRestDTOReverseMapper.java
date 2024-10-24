package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.validator.TwinflowTransitionValidatorRuleEntity;
import org.twins.core.dto.rest.twinflow.ValidatorUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class ValidatorUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<ValidatorUpdateDTOv1, TwinflowTransitionValidatorRuleEntity> {

    final ValidatorBaseV1RestDTOReverseMapper validatorBaseV1RestDTOReverseMapper;

    @Override
    public void map(ValidatorUpdateDTOv1 src, TwinflowTransitionValidatorRuleEntity dst, MapperContext mapperContext) throws Exception {
        validatorBaseV1RestDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }
}
