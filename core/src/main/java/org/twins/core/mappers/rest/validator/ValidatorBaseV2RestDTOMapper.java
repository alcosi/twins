package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.validator.TwinValidatorSetRepository;
import org.twins.core.dao.validator.Validator;
import org.twins.core.dto.rest.validator.ValidatorBaseDTOv1;
import org.twins.core.dto.rest.validator.ValidatorBaseDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TransitionMode;

@Component
@RequiredArgsConstructor
public class ValidatorBaseV2RestDTOMapper extends RestSimpleDTOMapper<Validator, ValidatorBaseDTOv2> {

    private final ValidatorBaseV1RestDTOMapper validatorBaseV1RestDTOMapper;
    private final TwinValidatorSetBaseV1RestDTOMapper twinValidatorSetBaseV1RestDTOMapper;
    private final TwinValidatorSetRepository twinValidatorSetRepository;

    @Override
    public void map(Validator src, ValidatorBaseDTOv2 dst, MapperContext mapperContext) throws Exception {
        validatorBaseV1RestDTOMapper.map(src, dst, mapperContext);
        dst
                .setTwinValidatorSet(twinValidatorSetBaseV1RestDTOMapper.convert(
                        twinValidatorSetRepository.findById(src.getTwinValidatorSetId()).orElse(null), mapperContext
                ));
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TransitionMode.HIDE);
    }

}
