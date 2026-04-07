package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.validator.TwinValidatorUpdate;
import org.twins.core.dto.rest.validator.TwinValidatorUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinValidatorUpdateDTOReverseMapper extends RestSimpleDTOMapper<TwinValidatorUpdateDTOv1, TwinValidatorUpdate> {
    private final TwinValidatorSaveDTOReverseMapper twinValidatorSaveDTOReverseMapper;

    @Override
    public void map(TwinValidatorUpdateDTOv1 src, TwinValidatorUpdate dst, MapperContext mapperContext) throws Exception {
        twinValidatorSaveDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }
}
