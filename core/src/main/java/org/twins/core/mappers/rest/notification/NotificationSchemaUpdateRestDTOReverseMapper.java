package org.twins.core.mappers.rest.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.domain.notification.NotificationSchemaUpdate;
import org.twins.core.dto.rest.notification.NotificationSchemaUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class NotificationSchemaUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<NotificationSchemaUpdateDTOv1, NotificationSchemaUpdate> {

    private final NotificationSchemaSaveRestDTOReverseMapper saveRestDTOReverseMapper;

    @Override
    public void map(NotificationSchemaUpdateDTOv1 src, NotificationSchemaUpdate dst, MapperContext mapperContext) throws Exception {
        saveRestDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }
}
