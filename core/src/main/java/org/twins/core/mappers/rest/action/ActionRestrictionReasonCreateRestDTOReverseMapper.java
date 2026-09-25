package org.twins.core.mappers.rest.action;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.action.ActionRestrictionReasonEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.domain.action.ActionRestrictionReasonCreate;
import org.twins.core.dto.rest.action.ActionRestrictionReasonCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class ActionRestrictionReasonCreateRestDTOReverseMapper extends RestSimpleDTOMapper<ActionRestrictionReasonCreateDTOv1, ActionRestrictionReasonCreate> {
    private final I18nSaveRestDTOReverseMapper i18nSaveRestDTOReverseMapper;

    @Override
    public void map(ActionRestrictionReasonCreateDTOv1 src, ActionRestrictionReasonCreate dst, MapperContext mapperContext) throws Exception {
        I18nEntity descriptionI18n = i18nSaveRestDTOReverseMapper.convert(src.getDescriptionI18n(), mapperContext);
        dst
                .setEntity(
                        new ActionRestrictionReasonEntity()
                                .setType(src.getType())
                )
                .setDescriptionI18n(descriptionI18n);
    }

}
