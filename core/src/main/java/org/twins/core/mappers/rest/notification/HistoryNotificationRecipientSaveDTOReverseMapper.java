package org.twins.core.mappers.rest.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.notification.HistoryNotificationRecipientEntity;
import org.twins.core.domain.notification.HistoryNotificationRecipientSave;
import org.twins.core.dto.rest.notification.HistoryNotificationRecipientSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.auth.AuthService;

import java.sql.Timestamp;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class HistoryNotificationRecipientSaveDTOReverseMapper extends RestSimpleDTOMapper<HistoryNotificationRecipientSaveDTOv1, HistoryNotificationRecipientSave> {
    private final I18nSaveRestDTOReverseMapper i18nSaveRestDTOReverseMapper;
    private final AuthService authService;

    @Override
    public void map(HistoryNotificationRecipientSaveDTOv1 src, HistoryNotificationRecipientSave dst, MapperContext mapperContext) throws Exception {
        I18nEntity nameI18N = i18nSaveRestDTOReverseMapper.convert(src.getNameI18n(), mapperContext);
        I18nEntity descriptionI18N = i18nSaveRestDTOReverseMapper.convert(src.getDescriptionI18n(), mapperContext);
        dst
                .setNameI18n(nameI18N)
                .setDescriptionI18n(descriptionI18N)
                .setHistoryNotificationRecipient(
                new HistoryNotificationRecipientEntity()
                        .setNameI18n(nameI18N)
                        .setDescriptionI18n(descriptionI18N)
                        .setCreatedByUserId(authService.getApiUser().getUserId())
                        .setCreatedAt(Timestamp.from(Instant.now())));
    }
}
