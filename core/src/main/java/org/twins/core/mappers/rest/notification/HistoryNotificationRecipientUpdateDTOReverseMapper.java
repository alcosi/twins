package org.twins.core.mappers.rest.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.notification.HistoryNotificationRecipientEntity;
import org.twins.core.domain.notification.HistoryNotificationRecipientUpdate;
import org.twins.core.dto.rest.notification.HistoryNotificationRecipientUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class HistoryNotificationRecipientUpdateDTOReverseMapper extends RestSimpleDTOMapper<HistoryNotificationRecipientUpdateDTOv1, HistoryNotificationRecipientUpdate> {
    private final I18nSaveRestDTOReverseMapper i18nSaveRestDTOReverseMapper;

    @Override
    public void map(HistoryNotificationRecipientUpdateDTOv1 src, HistoryNotificationRecipientUpdate dst, MapperContext mapperContext) throws Exception {
        I18nEntity nameI18N = i18nSaveRestDTOReverseMapper.convert(src.getNameI18n(), mapperContext);
        I18nEntity descriptionI18N = i18nSaveRestDTOReverseMapper.convert(src.getDescriptionI18n(), mapperContext);
        dst
                .setNameI18n(nameI18N)
                .setDescriptionI18n(descriptionI18N)
                .setHistoryNotificationRecipient(new HistoryNotificationRecipientEntity());
        dst.setId(src.getId());
    }
}
