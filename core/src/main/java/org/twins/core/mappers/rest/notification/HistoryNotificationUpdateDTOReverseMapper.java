package org.twins.core.mappers.rest.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.notification.HistoryNotificationUpdate;
import org.twins.core.dto.rest.notification.HistoryNotificationUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class HistoryNotificationUpdateDTOReverseMapper extends RestSimpleDTOMapper<HistoryNotificationUpdateDTOv1, HistoryNotificationUpdate> {
    private final HistoryNotificationSaveDTOReverseMapper historyNotificationSaveDTOReverseMapper;

    @Override
    public void map(HistoryNotificationUpdateDTOv1 src, HistoryNotificationUpdate dst, MapperContext mapperContext) throws Exception {
        historyNotificationSaveDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }
}
