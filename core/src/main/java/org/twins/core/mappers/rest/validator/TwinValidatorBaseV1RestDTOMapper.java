package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.dto.rest.validator.TwinValidatorBaseDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TransitionMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinValidatorMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {TwinValidatorMode.class})
public class TwinValidatorBaseV1RestDTOMapper extends RestSimpleDTOMapper<TwinValidatorEntity, TwinValidatorBaseDTOv1> {

    @Override
    public void map(TwinValidatorEntity src, TwinValidatorBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinValidatorMode.SHORT)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setOrder(src.getOrder())
                        .setDescription(src.getDescription())
                        .setValidatorFeaturerId(src.getTwinValidatorFeaturerId())
                        .setValidatorParams(src.getTwinValidatorParams())
                        .setInvert(src.isInvert())
                        .setActive(src.isActive())
                        .setTwinValidatorSetId(src.getTwinValidatorSetId());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setTwinValidatorSetId(src.getTwinValidatorSetId());
                break;
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinValidatorMode.HIDE);
    }

}
