package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.dto.rest.validator.TwinValidatorBaseDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TransitionMode;

@Component
@RequiredArgsConstructor
public class TwinValidatorBaseV1RestDTOMapper extends RestSimpleDTOMapper<TwinValidatorEntity, TwinValidatorBaseDTOv1> {

    @Override
    public void map(TwinValidatorEntity src, TwinValidatorBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setDescription(src.getDescription())
                .setValidatorFeaturerId(src.getTwinValidatorFeaturerId())
                .setValidatorParams(src.getTwinValidatorParams())
                .setInvert(src.isInvert())
                .setActive(src.isActive())
                .setActive(src.isActive()) //todo set
        ;
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TransitionMode.HIDE);
    }

}
