package org.twins.core.mappers.rest.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.notification.HistoryNotificationRecipientUpdate;
import org.twins.core.dto.rest.notification.HistoryNotificationRecipientUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class HistoryNotificationRecipientUpdateDTOReverseMapper extends RestSimpleDTOMapper<HistoryNotificationRecipientUpdateDTOv1, HistoryNotificationRecipientUpdate> {
    private final HistoryNotificationRecipientSaveDTOReverseMapper historyNotificationRecipientSaveDTOReverseMapper;

    @Override
    public void map(HistoryNotificationRecipientUpdateDTOv1 src, HistoryNotificationRecipientUpdate dst, MapperContext mapperContext) throws Exception {
        historyNotificationRecipientSaveDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }
}
