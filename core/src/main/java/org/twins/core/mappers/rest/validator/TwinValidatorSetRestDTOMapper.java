package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.validator.TwinValidatorSetEntity;
import org.twins.core.dto.rest.validator.TwinValidatorSetDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinValidatorSetMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {TwinValidatorSetMode.class})
public class TwinValidatorSetRestDTOMapper extends RestSimpleDTOMapper<TwinValidatorSetEntity, TwinValidatorSetDTOv1> {

    @Override
    public void map(TwinValidatorSetEntity src, TwinValidatorSetDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinValidatorSetMode.SHORT)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setName(src.getName())
                        .setDescription(src.getDescription())
                        .setInvert(src.getInvert());
                break;
            case SHORT:
                dst
                        .setId(src.getId());
                break;
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinValidatorSetMode.HIDE);
    }

}
