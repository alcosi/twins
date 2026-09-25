package org.twins.core.mappers.rest.notification;

import org.springframework.stereotype.Component;
import org.twins.core.dao.notification.HistoryNotificationEntity;
import org.twins.core.domain.notification.HistoryNotificationUpdate;
import org.twins.core.dto.rest.notification.HistoryNotificationUpdateDTOv1;
import org.twins.core.enums.history.HistoryType;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class HistoryNotificationUpdateDTOReverseMapper extends RestSimpleDTOMapper<HistoryNotificationUpdateDTOv1, HistoryNotificationUpdate> {

    @Override
    public void map(HistoryNotificationUpdateDTOv1 src, HistoryNotificationUpdate dst, MapperContext mapperContext) throws Exception {
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
        dst.setId(src.getId());
    }
}
