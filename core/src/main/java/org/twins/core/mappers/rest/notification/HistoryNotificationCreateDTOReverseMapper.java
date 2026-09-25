package org.twins.core.mappers.rest.notification;

import org.springframework.stereotype.Component;
import org.twins.core.dao.notification.HistoryNotificationEntity;
import org.twins.core.domain.notification.HistoryNotificationCreate;
import org.twins.core.dto.rest.notification.HistoryNotificationCreateDTOv1;
import org.twins.core.enums.history.HistoryType;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class HistoryNotificationCreateDTOReverseMapper extends RestSimpleDTOMapper<HistoryNotificationCreateDTOv1, HistoryNotificationCreate> {

    @Override
    public void map(HistoryNotificationCreateDTOv1 src, HistoryNotificationCreate dst, MapperContext mapperContext) throws Exception {
        dst.setHistoryNotification(
                new HistoryNotificationEntity()
                        .setHistoryTypeId(HistoryType.valueOd(src.getHistoryTypeId()))
                        .setTwinClassId(src.getTwinClassId())
                        .setTwinClassFieldId(src.getTwinClassFieldId())
                        .setTwinValidatorSetId(src.getTwinValidatorSetId())
                        .setTwinValidatorSetInvert(src.getTwinValidatorSetInvert())
                        .setNotificationSchemaId(src.getNotificationSchemaId())
                        .setHistoryNotificationRecipientId(src.getHistoryNotificationRecipientId())
                        .setNotificationChannelEventId(src.getNotificationChannelEventId())
                        .setActive(src.getActive())
        );
    }
}
