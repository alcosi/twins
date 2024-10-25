package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.validator.TwinValidatorSetRepository;
import org.twins.core.dao.validator.Validator;
import org.twins.core.dto.rest.validator.ValidatorRuleBaseDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinValidatorSetMode;
import org.twins.core.mappers.rest.mappercontext.modes.ValidatorRuleMode;
import org.twins.core.service.twin.TwinValidatorSetService;

@Component
@RequiredArgsConstructor
public class ValidatorRuleBaseV2RestDTOMapper extends RestSimpleDTOMapper<Validator, ValidatorRuleBaseDTOv2> {

    private final ValidatorRuleBaseV1RestDTOMapper validatorRuleBaseV1RestDTOMapper;
    @MapperModePointerBinding(modes = {TwinValidatorSetMode.ValidatorRule2TwinValidatorSetMode.class})
    private final TwinValidatorSetBaseV1RestDTOMapper twinValidatorSetBaseV1RestDTOMapper;
    private final TwinValidatorSetService twinValidatorSetService;

    @Override
    public void map(Validator src, ValidatorRuleBaseDTOv2 dst, MapperContext mapperContext) throws Exception {
        validatorRuleBaseV1RestDTOMapper.map(src, dst, mapperContext);
        switch (mapperContext.getModeOrUse(ValidatorRuleMode.SHORT)) {
            case DETAILED:
                dst
                .setTwinValidatorSet(twinValidatorSetBaseV1RestDTOMapper.convert(
                        twinValidatorSetService.loadTwinValidatorSet(src), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinValidatorSetMode.ValidatorRule2TwinValidatorSetMode.HIDE))))
                .setTwinValidatorSetId(src.getTwinValidatorSetId());
                break;
            case SHORT:
                dst
                        .setTwinValidatorSetId(src.getTwinValidatorSetId());
                break;
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(ValidatorRuleMode.HIDE);
    }
}
