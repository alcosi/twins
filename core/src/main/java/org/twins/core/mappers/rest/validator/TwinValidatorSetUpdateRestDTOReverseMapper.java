package org.twins.core.mappers.rest.validator;

import org.springframework.stereotype.Component;
import org.twins.core.dao.validator.TwinValidatorSetEntity;
import org.twins.core.domain.validator.TwinValidatorSetUpdate;
import org.twins.core.dto.rest.validator.TwinValidatorSetUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TwinValidatorSetUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinValidatorSetUpdateDTOv1, TwinValidatorSetUpdate> {

    @Override
    public void map(TwinValidatorSetUpdateDTOv1 src, TwinValidatorSetUpdate dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinValidatorSet(
                        new TwinValidatorSetEntity()
                                .setName(src.getName())
                                .setDescription(src.getDescription())
                                .setInvert(src.getInvert())
                );
        dst.setId(src.getId());
    }

}
