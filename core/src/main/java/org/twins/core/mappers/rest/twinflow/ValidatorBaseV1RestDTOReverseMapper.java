package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowTransitionValidatorEntity;
import org.twins.core.dto.rest.twinflow.ValidatorBaseDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class ValidatorBaseV1RestDTOReverseMapper extends RestSimpleDTOMapper<ValidatorBaseDTOv1, TwinflowTransitionValidatorEntity> {

    @Override
    public void map(ValidatorBaseDTOv1 src, TwinflowTransitionValidatorEntity dst, MapperContext mapperContext) throws Exception {
                dst
                        .setOrder(src.getOrder())
                        .setTwinValidatorFeaturerId(src.getValidatorFeaturerId())
                        .setTwinValidatorParams(src.getValidatorParams())
                        .setInvert(src.getInvert())
                        .setActive(src.getActive());
    }
}
