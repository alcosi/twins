package org.twins.core.mappers.rest.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.notification.HistoryNotificationSchemaMapEntity;
import org.twins.core.dto.rest.notification.HistoryNotificationSchemaMapDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class HistoryNotificationSchemaMapDTOMapperV1 extends RestSimpleDTOMapper<HistoryNotificationSchemaMapEntity, HistoryNotificationSchemaMapDTOv1> {

    @Override
    public void map(HistoryNotificationSchemaMapEntity src, HistoryNotificationSchemaMapDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setHistoryTypeId(src.getHistoryTypeId())
                .setTwinClassId(src.getTwinClassId())
                .setTwinClassFieldId(src.getTwinClassFieldId())
                .setTwinValidatorSetId(src.getTwinValidatorSetId())
                .setTwinValidatorSetInvert(src.getTwinValidatorSetInvert())
                .setNotificationSchemaId(src.getNotificationSchemaId())
                .setHistoryNotificationRecipientId(src.getHistoryNotificationRecipientId())
                .setNotificationChannelEventId(src.getNotificationChannelEventId());
    }
}
