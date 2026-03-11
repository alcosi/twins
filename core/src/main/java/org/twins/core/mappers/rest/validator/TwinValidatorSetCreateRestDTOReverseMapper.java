package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.validator.TwinValidatorSetCreate;
import org.twins.core.dto.rest.validator.TwinValidatorSetCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinValidatorSetCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinValidatorSetCreateDTOv1, TwinValidatorSetCreate> {
    private final TwinValidatorSetSaveRestDTOReverseMapper twinValidatorSetSaveRestDTOReverseMapper;

    @Override
    public void map(TwinValidatorSetCreateDTOv1 src, TwinValidatorSetCreate dst, MapperContext mapperContext) throws Exception {
        twinValidatorSetSaveRestDTOReverseMapper.map(src, dst, mapperContext);
    }

}
