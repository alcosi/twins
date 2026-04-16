package org.twins.core.mappers.rest.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.notification.HistoryNotificationCreate;
import org.twins.core.dto.rest.notification.HistoryNotificationCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class HistoryNotificationCreateDTOReverseMapper extends RestSimpleDTOMapper<HistoryNotificationCreateDTOv1, HistoryNotificationCreate> {
    private final HistoryNotificationSaveDTOReverseMapper historyNotificationSaveDTOReverseMapper;

    @Override
    public void map(HistoryNotificationCreateDTOv1 src, HistoryNotificationCreate dst, MapperContext mapperContext) throws Exception {
        historyNotificationSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}
