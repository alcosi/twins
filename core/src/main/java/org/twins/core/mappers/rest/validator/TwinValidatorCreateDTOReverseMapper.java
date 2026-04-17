package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.validator.TwinValidatorCreate;
import org.twins.core.dto.rest.validator.TwinValidatorCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinValidatorCreateDTOReverseMapper extends RestSimpleDTOMapper<TwinValidatorCreateDTOv1, TwinValidatorCreate> {
    private final TwinValidatorSaveDTOReverseMapper twinValidatorSaveDTOReverseMapper;

    @Override
    public void map(TwinValidatorCreateDTOv1 src, TwinValidatorCreate dst, MapperContext mapperContext) throws Exception {
        twinValidatorSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}
