package org.twins.core.mappers.rest.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.notification.HistoryNotificationSchemaMapEntity;
import org.twins.core.domain.notification.HistoryNotificationSchemaMapSave;
import org.twins.core.dto.rest.notification.HistoryNotificationSchemaMapSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class HistoryNotificationSchemaMapSaveDTOReverseMapper extends RestSimpleDTOMapper<HistoryNotificationSchemaMapSaveDTOv1, HistoryNotificationSchemaMapSave> {

    @Override
    public void map(HistoryNotificationSchemaMapSaveDTOv1 src, HistoryNotificationSchemaMapSave dst, MapperContext mapperContext) throws Exception {
        dst.setHistoryNotificationSchemaMap(
                new HistoryNotificationSchemaMapEntity()
                        .setHistoryTypeId(src.getHistoryTypeId())
                        .setTwinClassId(src.getTwinClassId())
                        .setTwinClassFieldId(src.getTwinClassFieldId())
                        .setTwinValidatorSetId(src.getTwinValidatorSetId())
                        .setTwinValidatorSetInvert(src.getTwinValidatorSetInvert())
                        .setNotificationSchemaId(src.getNotificationSchemaId())
                        .setHistoryNotificationRecipientId(src.getHistoryNotificationRecipientId())
                        .setNotificationChannelEventId(src.getNotificationChannelEventId())
        );
    }
}
