package org.twins.core.mappers.rest.action;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.action.ActionRestrictionReasonCreate;
import org.twins.core.dto.rest.action.ActionRestrictionReasonCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class ActionRestrictionReasonCreateRestDTOReverseMapper extends RestSimpleDTOMapper<ActionRestrictionReasonCreateDTOv1, ActionRestrictionReasonCreate> {
    private final ActionRestrictionReasonSaveRestDTOReverseMapper actionRestrictionReasonSaveRestDTOReverseMapper;

    @Override
    public void map(ActionRestrictionReasonCreateDTOv1 src, ActionRestrictionReasonCreate dst, MapperContext mapperContext) throws Exception {
        actionRestrictionReasonSaveRestDTOReverseMapper.map(src, dst, mapperContext);
    }

}
