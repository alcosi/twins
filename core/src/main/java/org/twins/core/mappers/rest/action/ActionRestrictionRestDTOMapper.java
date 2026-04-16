package org.twins.core.mappers.rest.action;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.action.ActionRestrictionReasonEntity;
import org.twins.core.dto.rest.twin.TwinActionRestrictionDTO;
import org.twins.core.enums.action.TwinAction;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ActionRestrictionRestDTOMapper extends RestSimpleDTOMapper<Map.Entry<TwinAction, ActionRestrictionReasonEntity>, TwinActionRestrictionDTO> {

    @Override
    public void map(Map.Entry<TwinAction, ActionRestrictionReasonEntity> src, TwinActionRestrictionDTO dst, MapperContext mapperContext) throws Exception {
        ActionRestrictionReasonEntity reason = src.getValue();
        dst.setAction(src.getKey())
           .setType(reason.getType())
           .setDescription(I18nCacheHolder.addId(reason.getDescriptionI18nId()));
    }
}
