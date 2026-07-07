package org.twins.core.mappers.rest.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.notification.NotificationSchemaCreate;
import org.twins.core.dto.rest.notification.NotificationSchemaCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class NotificationSchemaCreateRestDTOReverseMapper extends RestSimpleDTOMapper<NotificationSchemaCreateDTOv1, NotificationSchemaCreate> {
    private final NotificationSchemaSaveRestDTOReverseMapper saveRestDTOReverseMapper;

    @Override
    public void map(NotificationSchemaCreateDTOv1 src, NotificationSchemaCreate dst, MapperContext mapperContext) throws Exception {
        saveRestDTOReverseMapper.map(src, dst, mapperContext);
    }
}
