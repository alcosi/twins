package org.twins.core.mappers.rest.validator;

import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.validator.TwinValidatorSetEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.validator.TwinValidatorSetCountDTOv1;
import org.twins.core.enums.sort.TwinValidatorSetGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinValidatorSetMode;

import java.util.Collection;

@Component
@MapperModeBinding(modes = TwinValidatorSetMode.class)
public class TwinValidatorSetCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<TwinValidatorSetEntity, TwinValidatorSetGroupField>, TwinValidatorSetCountDTOv1> {

    @Override
    public void map(CountResult<TwinValidatorSetEntity, TwinValidatorSetGroupField> src, TwinValidatorSetCountDTOv1 dst, MapperContext mapperContext) {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setInvert(entity.getInvert())
                .setCount(src.getCount());
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<TwinValidatorSetEntity, TwinValidatorSetGroupField>> srcCollection, MapperContext mapperContext) {
        // no related objects to load — group field is a plain scalar (invert)
    }
}
