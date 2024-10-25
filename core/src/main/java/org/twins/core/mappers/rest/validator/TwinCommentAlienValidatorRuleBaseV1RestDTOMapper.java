package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.validator.TwinCommentActionAlienValidatorRuleEntity;
import org.twins.core.dto.rest.validator.TwinCommentAlienValidatorRuleBaseDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.ValidatorRuleMode;

@Component
@RequiredArgsConstructor
public class TwinCommentAlienValidatorRuleBaseV1RestDTOMapper extends RestSimpleDTOMapper<TwinCommentActionAlienValidatorRuleEntity, TwinCommentAlienValidatorRuleBaseDTOv1> {

    @MapperModePointerBinding(modes = {ValidatorRuleMode.TwinCommentActionAlienValidatorRule2ValidatorRuleMode.class})
    private final ValidatorRuleBaseV1RestDTOMapper validatorRuleBaseV1RestDTOMapper;

    @Override
    public void map(TwinCommentActionAlienValidatorRuleEntity src, TwinCommentAlienValidatorRuleBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
            validatorRuleBaseV1RestDTOMapper.map(src, dst, mapperContext.forkOnPoint(mapperContext.getModeOrUse(ValidatorRuleMode.TwinCommentActionAlienValidatorRule2ValidatorRuleMode.SHORT)));
            switch (mapperContext.getModeOrUse(ValidatorRuleMode.TwinflowTransitionValidatorRule2ValidatorRuleMode.SHORT)) {
                case DETAILED, SHORT:
                    dst.setTwinClassId(src.getTwinClassId())
                            .setCommentAction(src.getTwinCommentAction());
                    break;
            }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(ValidatorRuleMode.TwinCommentActionAlienValidatorRule2ValidatorRuleMode.HIDE);
    }

}
