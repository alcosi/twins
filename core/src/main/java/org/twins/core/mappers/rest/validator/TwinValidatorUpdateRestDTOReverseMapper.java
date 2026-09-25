package org.twins.core.mappers.rest.validator;

import org.springframework.stereotype.Component;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.domain.validator.TwinValidatorUpdate;
import org.twins.core.dto.rest.validator.TwinValidatorUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TwinValidatorUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinValidatorUpdateDTOv1, TwinValidatorUpdate> {

    @Override
    public void map(TwinValidatorUpdateDTOv1 src, TwinValidatorUpdate dst, MapperContext mapperContext) throws Exception {
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
        dst.setId(src.getId());
    }

}
