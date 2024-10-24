package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.validator.TwinflowTransitionValidatorRuleEntity;
import org.twins.core.dto.rest.validator.ValidatorDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TransitionMode;

@Component
@RequiredArgsConstructor
public class ValidatorV1RestDTOMapper extends RestSimpleDTOMapper<TwinflowTransitionValidatorRuleEntity, ValidatorDTOv1> {

    private final ValidatorRuleBaseV1RestDTOMapper validatorRuleBaseV1RestDTOMapper;
//TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    @Override
    public void map(TwinflowTransitionValidatorRuleEntity src, ValidatorDTOv1 dst, MapperContext mapperContext) throws Exception {
        validatorRuleBaseV1RestDTOMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }

    @Override
    public String getObjectCacheId(TwinflowTransitionValidatorRuleEntity src) {
        return src.getId().toString();
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TransitionMode.HIDE);
    }

}
