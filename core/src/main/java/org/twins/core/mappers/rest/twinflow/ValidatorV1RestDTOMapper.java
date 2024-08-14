package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowTransitionValidatorEntity;
import org.twins.core.dto.rest.twinflow.ValidatorDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TransitionMode;

@Component
@RequiredArgsConstructor
public class ValidatorV1RestDTOMapper extends RestSimpleDTOMapper<TwinflowTransitionValidatorEntity, ValidatorDTOv1> {

    private final ValidatorBaseV1RestDTOMapper validatorBaseV1RestDTOMapper;

    @Override
    public void map(TwinflowTransitionValidatorEntity src, ValidatorDTOv1 dst, MapperContext mapperContext) throws Exception {
        validatorBaseV1RestDTOMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }

    @Override
    public String getObjectCacheId(TwinflowTransitionValidatorEntity src) {
        return src.getId().toString();
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TransitionMode.HIDE);
    }

}
