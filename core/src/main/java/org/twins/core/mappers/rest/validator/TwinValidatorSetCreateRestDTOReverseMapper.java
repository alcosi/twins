package org.twins.core.mappers.rest.validator;

import org.springframework.stereotype.Component;
import org.twins.core.dao.validator.TwinValidatorSetEntity;
import org.twins.core.domain.validator.TwinValidatorSetCreate;
import org.twins.core.dto.rest.validator.TwinValidatorSetCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TwinValidatorSetCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinValidatorSetCreateDTOv1, TwinValidatorSetCreate> {

    @Override
    public void map(TwinValidatorSetCreateDTOv1 src, TwinValidatorSetCreate dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinValidatorSet(
                        new TwinValidatorSetEntity()
                                .setName(src.getName())
                                .setDescription(src.getDescription())
                                .setInvert(src.getInvert())
                );
    }

}
