package org.twins.core.mappers.rest.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.notification.HistoryNotificationSchemaMapCreate;
import org.twins.core.dto.rest.notification.HistoryNotificationSchemaMapCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class HistoryNotificationSchemaMapCreateDTOReverseMapper extends RestSimpleDTOMapper<HistoryNotificationSchemaMapCreateDTOv1, HistoryNotificationSchemaMapCreate> {
    private final HistoryNotificationSchemaMapSaveDTOReverseMapper historyNotificationSchemaMapSaveDTOReverseMapper;

    @Override
    public void map(HistoryNotificationSchemaMapCreateDTOv1 src, HistoryNotificationSchemaMapCreate dst, MapperContext mapperContext) throws Exception {
        historyNotificationSchemaMapSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}
