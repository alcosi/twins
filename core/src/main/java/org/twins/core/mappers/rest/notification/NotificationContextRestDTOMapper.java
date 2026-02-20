package org.twins.core.mappers.rest.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.notification.NotificationContextEntity;
import org.twins.core.dto.rest.notification.NotificationContextDTOv1;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.NotificationContextMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = NotificationContextMode.class)
public class NotificationContextRestDTOMapper extends RestSimpleDTOMapper<NotificationContextEntity, NotificationContextDTOv1> {

    @Override
    public void map(NotificationContextEntity src, NotificationContextDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(NotificationContextMode.DETAILED)) {
            case DETAILED:
                dst
                    .setId(src.getId())
                    .setNameI18nId(src.getNameI18nId())
                    .setDescriptionI18nId(src.getDescriptionI18nId())
                    .setName(I18nCacheHolder.addId(src.getNameI18nId()))
                    .setDescription(I18nCacheHolder.addId(src.getDescriptionI18nId()));
            case SHORT:
                dst
                    .setId(src.getId())
                    .setNameI18nId(src.getNameI18nId())
                    .setDescriptionI18nId(src.getDescriptionI18nId());
        }
    }
}
