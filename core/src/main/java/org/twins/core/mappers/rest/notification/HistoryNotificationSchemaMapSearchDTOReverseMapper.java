package org.twins.core.mappers.rest.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.HistoryNotificationSchemaMapSearch;
import org.twins.core.dto.rest.notification.HistoryNotificationSchemaMapSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class HistoryNotificationSchemaMapSearchDTOReverseMapper extends RestSimpleDTOMapper<HistoryNotificationSchemaMapSearchRqDTOv1, HistoryNotificationSchemaMapSearch> {

    @Override
    public void map(HistoryNotificationSchemaMapSearchRqDTOv1 src, HistoryNotificationSchemaMapSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setHistoryTypeIdList(src.getHistoryTypeIdList())
                .setHistoryTypeIdExcludeList(src.getHistoryTypeIdExcludeList())
                .setTwinClassIdList(src.getTwinClassIdList())
                .setTwinClassIdExcludeList(src.getTwinClassIdExcludeList())
                .setTwinClassFieldIdList(src.getTwinClassFieldIdList())
                .setTwinClassFieldIdExcludeList(src.getTwinClassFieldIdExcludeList())
                .setTwinValidatorSetIdList(src.getTwinValidatorSetIdList())
                .setTwinValidatorSetIdExcludeList(src.getTwinValidatorSetIdExcludeList())
                .setTwinValidatorSetInvert(src.getTwinValidatorSetInvert())
                .setNotificationSchemaIdList(src.getNotificationSchemaIdList())
                .setNotificationSchemaIdExcludeList(src.getNotificationSchemaIdExcludeList())
                .setHistoryNotificationRecipientIdList(src.getHistoryNotificationRecipientIdList())
                .setHistoryNotificationRecipientIdExcludeList(src.getHistoryNotificationRecipientIdExcludeList())
                .setNotificationChannelEventIdList(src.getNotificationChannelEventIdList())
                .setNotificationChannelEventIdExcludeList(src.getNotificationChannelEventIdExcludeList());
    }
}
