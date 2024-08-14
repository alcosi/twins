package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowTransitionValidatorEntity;
import org.twins.core.dto.rest.twinflow.ValidatorBaseDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TransitionMode;

@Component
@RequiredArgsConstructor
public class ValidatorBaseV1RestDTOMapper extends RestSimpleDTOMapper<TwinflowTransitionValidatorEntity, ValidatorBaseDTOv1> {

    @Override
    public void map(TwinflowTransitionValidatorEntity src, ValidatorBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
                dst
                        .setOrder(src.getOrder())
                        .setValidatorFeaturerId(src.getTwinValidatorFeaturerId())
                        .setValidatorParams(src.getTwinValidatorParams())
                        .setInvert(src.isInvert())
                        .setActive(src.isActive());
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
