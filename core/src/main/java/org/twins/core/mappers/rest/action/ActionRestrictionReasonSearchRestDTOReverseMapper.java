package org.twins.core.mappers.rest.action;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dto.rest.action.ActionRestrictionReasonSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.ActionRestrictionReasonMode;
import org.twins.core.domain.search.ActionRestrictionReasonSearch;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = ActionRestrictionReasonMode.class)
public class ActionRestrictionReasonSearchRestDTOReverseMapper extends RestSimpleDTOMapper<ActionRestrictionReasonSearchDTOv1, ActionRestrictionReasonSearch> {

    @Override
    public void map(ActionRestrictionReasonSearchDTOv1 src, ActionRestrictionReasonSearch dst, MapperContext mapperContext) {
        dst.setIdList(src.idList);
        dst.setIdExcludeList(src.idExcludeList);
        dst.setTypeLikeList(src.typeLikeList);
        dst.setTypeNotLikeList(src.typeNotLikeList);
        dst.setDescriptionLikeList(src.descriptionLikeList);
        dst.setDescriptionNotLikeList(src.descriptionNotLikeList);
    }
}
