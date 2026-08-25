package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.validator.TwinValidatorCreate;
import org.twins.core.dto.rest.validator.TwinValidatorCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinValidatorCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinValidatorCreateDTOv1, TwinValidatorCreate> {
    private final TwinValidatorSaveRestDTOReverseMapper twinValidatorSaveRestDTOReverseMapper;

    @Override
    public void map(TwinValidatorCreateDTOv1 src, TwinValidatorCreate dst, MapperContext mapperContext) throws Exception {
        twinValidatorSaveRestDTOReverseMapper.map(src, dst, mapperContext);
    }

}
