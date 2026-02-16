package org.twins.core.mappers.rest.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.notification.HistoryNotificationRecipientCollectorUpdate;
import org.twins.core.dto.rest.notification.HistoryNotificationRecipientCollectorUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@RequiredArgsConstructor
@Component
public class HistoryNotificationRecipientCollectorUpdateDTOReverseMapper extends RestSimpleDTOMapper<HistoryNotificationRecipientCollectorUpdateDTOv1, HistoryNotificationRecipientCollectorUpdate> {
    private final HistoryNotificationRecipientCollectorSaveDTOReverseMapper historyNotificationRecipientCollectorSaveDTOReverseMapper;

    @Override
    public void map(HistoryNotificationRecipientCollectorUpdateDTOv1 src, HistoryNotificationRecipientCollectorUpdate dst, MapperContext mapperContext) throws Exception {
        historyNotificationRecipientCollectorSaveDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }
}
