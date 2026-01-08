package org.twins.core.mappers.rest.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.notification.HistoryNotificationRecipientEntity;
import org.twins.core.dto.rest.notification.HistoryNotificationRecipientDTOv1;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DataListOptionMode;
import org.twins.core.mappers.rest.mappercontext.modes.HistoryNotificationRecipientMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = HistoryNotificationRecipientMode.class)
public class HistoryNotificationRecipientDTOMapperV1 extends RestSimpleDTOMapper<HistoryNotificationRecipientEntity, HistoryNotificationRecipientDTOv1> {

    @Override
    public void map(HistoryNotificationRecipientEntity src, HistoryNotificationRecipientDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(HistoryNotificationRecipientMode.DETAILED)) {
            case DETAILED ->
                    dst
                            .setId(src.getId())
                            .setName(I18nCacheHolder.addId(src.getNameI18nId()))
                            .setDescription(I18nCacheHolder.addId(src.getDescriptionI18nId()))
                            .setCreatedByUserId(src.getCreatedByUserId())
                            .setCreatedAt(src.getCreatedAt().toLocalDateTime());
            case SHORT ->
                    dst
                            .setId(src.getId())
                            .setName(I18nCacheHolder.addId(src.getNameI18nId()));
        }
    }
}
