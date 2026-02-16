package org.twins.core.mappers.rest.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.notification.HistoryNotificationRecipientCollectorCreate;
import org.twins.core.dto.rest.notification.HistoryNotificationRecipientCollectorCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@RequiredArgsConstructor
@Component
public class HistoryNotificationRecipientCollectorCreateDTOReverseMapper extends RestSimpleDTOMapper<HistoryNotificationRecipientCollectorCreateDTOv1, HistoryNotificationRecipientCollectorCreate> {
    private final HistoryNotificationRecipientCollectorSaveDTOReverseMapper historyNotificationRecipientCollectorSaveDTOReverseMapper;
    @Override
    public void map(HistoryNotificationRecipientCollectorCreateDTOv1 src, HistoryNotificationRecipientCollectorCreate dst, MapperContext mapperContext) throws Exception {
        historyNotificationRecipientCollectorSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}
