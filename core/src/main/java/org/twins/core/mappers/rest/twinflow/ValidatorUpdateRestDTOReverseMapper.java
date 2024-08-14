package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowTransitionValidatorEntity;
import org.twins.core.dto.rest.twinflow.ValidatorUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class ValidatorUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<ValidatorUpdateDTOv1, TwinflowTransitionValidatorEntity> {

    final ValidatorBaseV1RestDTOReverseMapper validatorBaseV1RestDTOReverseMapper;

    @Override
    public void map(ValidatorUpdateDTOv1 src, TwinflowTransitionValidatorEntity dst, MapperContext mapperContext) throws Exception {
        validatorBaseV1RestDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }
}
