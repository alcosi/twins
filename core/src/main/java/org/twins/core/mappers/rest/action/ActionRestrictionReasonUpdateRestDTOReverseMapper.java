package org.twins.core.mappers.rest.action;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.action.ActionRestrictionReasonUpdate;
import org.twins.core.dto.rest.action.ActionRestrictionReasonUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class ActionRestrictionReasonUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<ActionRestrictionReasonUpdateDTOv1, ActionRestrictionReasonUpdate> {
    private final ActionRestrictionReasonSaveRestDTOReverseMapper actionRestrictionReasonSaveRestDTOReverseMapper;

    @Override
    public void map(ActionRestrictionReasonUpdateDTOv1 src, ActionRestrictionReasonUpdate dst, MapperContext mapperContext) throws Exception {
        actionRestrictionReasonSaveRestDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }

}
