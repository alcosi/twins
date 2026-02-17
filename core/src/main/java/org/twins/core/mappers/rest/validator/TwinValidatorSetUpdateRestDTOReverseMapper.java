package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.validator.TwinValidatorSetUpdate;
import org.twins.core.dto.rest.validator.TwinValidatorSetUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinValidatorSetUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinValidatorSetUpdateDTOv1, TwinValidatorSetUpdate> {
    private final TwinValidatorSetSaveRestDTOReverseMapper twinValidatorSetSaveRestDTOReverseMapper;

    @Override
    public void map(TwinValidatorSetUpdateDTOv1 src, TwinValidatorSetUpdate dst, MapperContext mapperContext) throws Exception {
        twinValidatorSetSaveRestDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }

}
