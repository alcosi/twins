package org.twins.core.mappers.rest.action;

import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.action.ActionRestrictionReasonEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.action.ActionRestrictionReasonCountDTOv1;
import org.twins.core.enums.sort.ActionRestrictionReasonGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.ActionRestrictionReasonMode;

import java.util.Collection;

@Component
@MapperModeBinding(modes = ActionRestrictionReasonMode.class)
public class ActionRestrictionReasonCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<ActionRestrictionReasonEntity, ActionRestrictionReasonGroupField>, ActionRestrictionReasonCountDTOv1> {

    @Override
    public void map(CountResult<ActionRestrictionReasonEntity, ActionRestrictionReasonGroupField> src, ActionRestrictionReasonCountDTOv1 dst, MapperContext mapperContext) {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setType(entity.getType())
                .setCount(src.getCount());
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<ActionRestrictionReasonEntity, ActionRestrictionReasonGroupField>> srcCollection, MapperContext mapperContext) {
        // no related objects to load — group field is a plain scalar (type)
    }
}
