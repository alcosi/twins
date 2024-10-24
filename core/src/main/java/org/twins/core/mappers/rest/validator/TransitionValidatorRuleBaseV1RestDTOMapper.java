package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.validator.TwinflowTransitionValidatorRuleEntity;
import org.twins.core.dto.rest.validator.TransitionValidatorRuleBaseDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.ValidatorRuleMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {ValidatorRuleMode.TwinflowTransitionValidatorRule2ValidatorRuleMode.class})
public class TransitionValidatorRuleBaseV1RestDTOMapper extends RestSimpleDTOMapper<TwinflowTransitionValidatorRuleEntity, TransitionValidatorRuleBaseDTOv1> {

    private final ValidatorRuleBaseV1RestDTOMapper validatorRuleBaseV1RestDTOMapper;

    @Override
    public void map(TwinflowTransitionValidatorRuleEntity src, TransitionValidatorRuleBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
            validatorRuleBaseV1RestDTOMapper.map(src, dst, mapperContext.forkOnPoint(mapperContext.getModeOrUse(ValidatorRuleMode.TwinflowTransitionValidatorRule2ValidatorRuleMode.SHORT)));
            switch (mapperContext.getModeOrUse(ValidatorRuleMode.TwinflowTransitionValidatorRule2ValidatorRuleMode.SHORT)) {
                case DETAILED, SHORT:
                    dst.setTwinflowTransitionId(src.getId());
                    break;
            }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(ValidatorRuleMode.TwinflowTransitionValidatorRule2ValidatorRuleMode.HIDE);
    }

}
