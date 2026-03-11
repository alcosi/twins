package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.validator.TwinValidatorSetEntity;
import org.twins.core.domain.validator.TwinValidatorSetSave;
import org.twins.core.dto.rest.validator.TwinValidatorSetSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinValidatorSetSaveRestDTOReverseMapper extends RestSimpleDTOMapper<TwinValidatorSetSaveDTOv1, TwinValidatorSetSave> {

    @Override
    public void map(TwinValidatorSetSaveDTOv1 src, TwinValidatorSetSave dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinValidatorSet(
                        new TwinValidatorSetEntity()
                                .setName(src.getName())
                                .setDescription(src.getDescription())
                                .setInvert(src.getInvert())
                );
    }

}
