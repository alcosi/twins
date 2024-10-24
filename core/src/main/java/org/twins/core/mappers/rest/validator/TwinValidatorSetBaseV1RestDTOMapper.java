package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.validator.TwinValidatorSetEntity;
import org.twins.core.dto.rest.validator.TwinValidatorSetBaseDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TransitionMode;

@Component
@RequiredArgsConstructor
public class TwinValidatorSetBaseV1RestDTOMapper extends RestSimpleDTOMapper<TwinValidatorSetEntity, TwinValidatorSetBaseDTOv1> {

    @Override
    public void map(TwinValidatorSetEntity src, TwinValidatorSetBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setName(src.getName())
                .setDescription(src.getDescription());
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TransitionMode.HIDE);
    }

}
