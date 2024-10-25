package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.validator.TwinflowTransitionValidatorRuleEntity;
import org.twins.core.dto.rest.validator.cud.ValidatorUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class ValidatorUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<ValidatorUpdateDTOv1, TwinflowTransitionValidatorRuleEntity> {

    final TwinValidatorBaseV1RestDTOReverseMapper twinValidatorBaseV1RestDTOReverseMapper;

    //todo think about cud logic

    @Override
    public void map(ValidatorUpdateDTOv1 src, TwinflowTransitionValidatorRuleEntity dst, MapperContext mapperContext) throws Exception {
//        twinValidatorBaseV1RestDTOReverseMapper.map(src, dst, mapperContext);
//        dst.setId(src.getId());
    }
}
