package org.twins.core.mappers.rest.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.notification.HistoryNotificationSchemaMapUpdate;
import org.twins.core.dto.rest.notification.HistoryNotificationSchemaMapUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class HistoryNotificationSchemaMapUpdateDTOReverseMapper extends RestSimpleDTOMapper<HistoryNotificationSchemaMapUpdateDTOv1, HistoryNotificationSchemaMapUpdate> {
    private final HistoryNotificationSchemaMapSaveDTOReverseMapper historyNotificationSchemaMapSaveDTOReverseMapper;

    @Override
    public void map(HistoryNotificationSchemaMapUpdateDTOv1 src, HistoryNotificationSchemaMapUpdate dst, MapperContext mapperContext) throws Exception {
        historyNotificationSchemaMapSaveDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }
}
