package org.twins.core.mappers.rest.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.notification.HistoryNotificationRecipientCreate;
import org.twins.core.dto.rest.notification.HistoryNotificationRecipientCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@RequiredArgsConstructor
@Component
public class HistoryNotificationRecipientCreateDTOReverseMapper extends RestSimpleDTOMapper<HistoryNotificationRecipientCreateDTOv1, HistoryNotificationRecipientCreate> {
    private final HistoryNotificationRecipientSaveDTOReverseMapper historyNotificationRecipientSaveDTOReverseMapper;

    @Override
    public void map(HistoryNotificationRecipientCreateDTOv1 src, HistoryNotificationRecipientCreate dst, MapperContext mapperContext) throws Exception {
        historyNotificationRecipientSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}
