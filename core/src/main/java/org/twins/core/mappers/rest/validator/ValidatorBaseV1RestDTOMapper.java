package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.validator.Validator;
import org.twins.core.dto.rest.validator.ValidatorBaseDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TransitionMode;

@Component
@RequiredArgsConstructor
public class ValidatorBaseV1RestDTOMapper extends RestSimpleDTOMapper<Validator, ValidatorBaseDTOv1> {

    private final TwinValidatorBaseV1RestDTOMapper twinValidatorBaseV1RestDTOMapper;


    @Override
    public void map(Validator src, ValidatorBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setTwinValidatorSetId(src.getTwinValidatorSetId())
                .setOrder(src.getOrder())
                .setTwinValidators(twinValidatorBaseV1RestDTOMapper.convertCollection(src.getTwinValidators()));
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TransitionMode.HIDE);
    }

}
