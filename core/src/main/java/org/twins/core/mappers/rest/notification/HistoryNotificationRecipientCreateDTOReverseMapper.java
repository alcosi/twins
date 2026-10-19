package org.twins.core.mappers.rest.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.notification.HistoryNotificationRecipientEntity;
import org.twins.core.domain.notification.HistoryNotificationRecipientCreate;
import org.twins.core.dto.rest.notification.HistoryNotificationRecipientCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@RequiredArgsConstructor
@Component
public class HistoryNotificationRecipientCreateDTOReverseMapper extends RestSimpleDTOMapper<HistoryNotificationRecipientCreateDTOv1, HistoryNotificationRecipientCreate> {
    private final I18nSaveRestDTOReverseMapper i18nSaveRestDTOReverseMapper;

    @Override
    public void map(HistoryNotificationRecipientCreateDTOv1 src, HistoryNotificationRecipientCreate dst, MapperContext mapperContext) throws Exception {
        I18nEntity nameI18N = i18nSaveRestDTOReverseMapper.convert(src.getNameI18n(), mapperContext);
        I18nEntity descriptionI18N = i18nSaveRestDTOReverseMapper.convert(src.getDescriptionI18n(), mapperContext);
        dst
                .setNameI18n(nameI18N)
                .setDescriptionI18n(descriptionI18N)
                .setHistoryNotificationRecipient(new HistoryNotificationRecipientEntity());
    }
}
