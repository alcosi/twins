package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.validator.TwinActionValidatorRuleEntity;
import org.twins.core.dto.rest.validator.TwinActionValidatorRuleBaseDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.ValidatorRuleMode;

@Component
@RequiredArgsConstructor
public class TwinActionValidatorRuleBaseV1RestDTOMapper extends RestSimpleDTOMapper<TwinActionValidatorRuleEntity, TwinActionValidatorRuleBaseDTOv1> {

    @MapperModePointerBinding(modes = {ValidatorRuleMode.TwinActionValidatorRule2ValidatorRuleMode.class})
    private final ValidatorRuleBaseV1RestDTOMapper validatorRuleBaseV1RestDTOMapper;

    @Override
    public void map(TwinActionValidatorRuleEntity src, TwinActionValidatorRuleBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
        validatorRuleBaseV1RestDTOMapper.map(src, dst, mapperContext.forkOnPoint(mapperContext.getModeOrUse(ValidatorRuleMode.TwinflowTransitionValidatorRule2ValidatorRuleMode.SHORT)));
        switch (mapperContext.getModeOrUse(ValidatorRuleMode.TwinActionValidatorRule2ValidatorRuleMode.SHORT)) {
            case DETAILED, SHORT:
                dst.setTwinClassId(src.getTwinClassId())
                        .setTwinAction(src.getTwinAction());
                break;
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(ValidatorRuleMode.TwinActionValidatorRule2ValidatorRuleMode.HIDE);
    }

}
