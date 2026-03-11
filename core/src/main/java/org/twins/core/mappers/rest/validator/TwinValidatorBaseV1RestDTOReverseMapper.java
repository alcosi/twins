package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.dto.rest.validator.TwinValidatorDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinValidatorBaseV1RestDTOReverseMapper extends RestSimpleDTOMapper<TwinValidatorDTOv1, TwinValidatorEntity> {

    @Override
    public void map(TwinValidatorDTOv1 src, TwinValidatorEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setTwinValidatorSetId(src.getTwinValidatorSetId())
                .setTwinValidatorFeaturerId(src.getValidatorFeaturerId())
                .setTwinValidatorParams(src.getValidatorParams())
                .setOrder(src.getOrder())
                .setInvert(src.getInvert())
                .setActive(src.getActive())
                .setDescription(src.getDescription());
    }
}
