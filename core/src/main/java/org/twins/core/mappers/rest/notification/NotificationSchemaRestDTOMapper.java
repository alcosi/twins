package org.twins.core.mappers.rest.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.notification.NotificationSchemaEntity;
import org.twins.core.dto.rest.notification.NotificationSchemaDTOv1;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.NotificationSchemaMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = NotificationSchemaMode.class)
public class NotificationSchemaRestDTOMapper extends RestSimpleDTOMapper<NotificationSchemaEntity, NotificationSchemaDTOv1> {

    @Override
    public void map(NotificationSchemaEntity src, NotificationSchemaDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(NotificationSchemaMode.DETAILED)) {
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
