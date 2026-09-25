package org.twins.core.mappers.rest.validator;

import org.springframework.stereotype.Component;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.domain.validator.TwinValidatorCreate;
import org.twins.core.dto.rest.validator.TwinValidatorCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TwinValidatorCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinValidatorCreateDTOv1, TwinValidatorCreate> {

    @Override
    public void map(TwinValidatorCreateDTOv1 src, TwinValidatorCreate dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinValidator(
                        new TwinValidatorEntity()
                                .setTwinValidatorSetId(src.getTwinValidatorSetId())
                                .setTwinValidatorFeaturerId(src.getValidatorFeaturerId())
                                .setTwinValidatorParams(src.getValidatorParams())
                                .setInvert(src.getInvert())
                                .setActive(src.getActive())
                                .setDescription(src.getDescription())
                                .setOrder(src.getOrder())
                );
    }

}
