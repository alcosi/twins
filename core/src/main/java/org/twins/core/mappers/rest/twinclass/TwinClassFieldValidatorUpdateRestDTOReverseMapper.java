package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.twinclass.TwinClassFieldValidatorUpdate;
import org.twins.core.dto.rest.twinclass.TwinClassFieldValidatorUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinClassFieldValidatorUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFieldValidatorUpdateDTOv1, TwinClassFieldValidatorUpdate> {
    private final TwinClassFieldValidatorSaveRestDTOReverseMapper twinClassFieldValidatorSaveRestDTOReverseMapper;

    @Override
    public void map(TwinClassFieldValidatorUpdateDTOv1 src, TwinClassFieldValidatorUpdate dst, MapperContext mapperContext) throws Exception {
        twinClassFieldValidatorSaveRestDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }
}
