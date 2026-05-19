package org.twins.core.mappers.rest.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.notification.NotificationSchemaEntity;
import org.twins.core.domain.notification.NotificationSchemaSave;
import org.twins.core.dto.rest.notification.NotificationSchemaSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class NotificationSchemaSaveRestDTOReverseMapper extends RestSimpleDTOMapper<NotificationSchemaSaveDTOv1, NotificationSchemaSave> {
    private final I18nSaveRestDTOReverseMapper i18nSaveRestDTOReverseMapper;

    @Override
    public void map(NotificationSchemaSaveDTOv1 src, NotificationSchemaSave dst, MapperContext mapperContext) throws Exception {
        I18nEntity nameI18n = i18nSaveRestDTOReverseMapper.convert(src.getNameI18n(), mapperContext);
        I18nEntity descriptionI18n = i18nSaveRestDTOReverseMapper.convert(src.getDescriptionI18n(), mapperContext);
        dst
                .setNameI18n(nameI18n)
                .setDescriptionI18n(descriptionI18n)
                .setNotificationSchema(
                new NotificationSchemaEntity()
                        .setNameI18n(nameI18n)
                        .setDescriptionI18n(descriptionI18n));
    }
}
