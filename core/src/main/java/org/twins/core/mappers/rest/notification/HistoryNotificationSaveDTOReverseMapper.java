package org.twins.core.mappers.rest.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.notification.HistoryNotificationEntity;
import org.twins.core.domain.notification.HistoryNotificationSave;
import org.twins.core.dto.rest.notification.HistoryNotificationSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class HistoryNotificationSaveDTOReverseMapper extends RestSimpleDTOMapper<HistoryNotificationSaveDTOv1, HistoryNotificationSave> {

    @Override
    public void map(HistoryNotificationSaveDTOv1 src, HistoryNotificationSave dst, MapperContext mapperContext) throws Exception {
        dst.setHistoryNotification(
                new HistoryNotificationEntity()
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
