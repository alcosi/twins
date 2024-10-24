package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.validator.Validator;
import org.twins.core.dto.rest.validator.ValidatorRuleBaseDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = { ValidatorRuleMode.class })
public class ValidatorRuleBaseV1RestDTOMapper extends RestSimpleDTOMapper<Validator, ValidatorRuleBaseDTOv1> {

    @MapperModePointerBinding(modes = {TwinValidatorMode.ValidatorRule2TwinValidatorMode.class})
    private final TwinValidatorBaseV1RestDTOMapper twinValidatorBaseV1RestDTOMapper;


    @Override
    public void map(Validator src, ValidatorRuleBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(ValidatorRuleMode.SHORT)) {
            case DETAILED:
                dst
                .setId(src.getId())
                .setOrder(src.getOrder())
                .setTwinValidators(twinValidatorBaseV1RestDTOMapper.convertCollection(src.getTwinValidators(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinValidatorMode.ValidatorRule2TwinValidatorMode.SHORT))));
                break;
            case SHORT:
                dst
                        .setId(src.getId());
                break;
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(ValidatorRuleMode.HIDE);
    }

}
