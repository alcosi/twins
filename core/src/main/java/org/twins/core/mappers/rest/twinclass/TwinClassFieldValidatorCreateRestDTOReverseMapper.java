package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.twinclass.TwinClassFieldValidatorCreate;
import org.twins.core.dto.rest.twinclass.TwinClassFieldValidatorCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinClassFieldValidatorCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFieldValidatorCreateDTOv1, TwinClassFieldValidatorCreate> {
    private final TwinClassFieldValidatorSaveRestDTOReverseMapper twinClassFieldValidatorSaveRestDTOReverseMapper;

    @Override
    public void map(TwinClassFieldValidatorCreateDTOv1 src, TwinClassFieldValidatorCreate dst, MapperContext mapperContext) throws Exception {
        twinClassFieldValidatorSaveRestDTOReverseMapper.map(src, dst, mapperContext);
    }
}
