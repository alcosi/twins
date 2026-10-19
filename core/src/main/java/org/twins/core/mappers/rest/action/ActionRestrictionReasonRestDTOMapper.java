package org.twins.core.mappers.rest.action;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.action.ActionRestrictionReasonEntity;
import org.twins.core.dto.rest.action.ActionRestrictionReasonDTOv1;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.ActionRestrictionReasonMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = ActionRestrictionReasonMode.class)
public class ActionRestrictionReasonRestDTOMapper extends RestSimpleDTOMapper<ActionRestrictionReasonEntity, ActionRestrictionReasonDTOv1> {

    @Override
    public void map(ActionRestrictionReasonEntity src, ActionRestrictionReasonDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(ActionRestrictionReasonMode.DETAILED)) {
            case DETAILED -> dst
                    .setId(src.getId())
                    .setType(src.getType())
                    .setDescription(I18nCacheHolder.addId(src.getDescriptionI18nId()));

            case SHORT -> dst
                    .setId(src.getId())
                    .setType(src.getType());
        }
    }
}
