package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.domain.validator.TwinValidatorSave;
import org.twins.core.dto.rest.validator.TwinValidatorSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinValidatorSaveRestDTOReverseMapper extends RestSimpleDTOMapper<TwinValidatorSaveDTOv1, TwinValidatorSave> {

    @Override
    public void map(TwinValidatorSaveDTOv1 src, TwinValidatorSave dst, MapperContext mapperContext) throws Exception {
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
