package org.twins.core.mappers.rest.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.notification.NotificationChannelEntity;
import org.twins.core.dto.rest.notification.NotificationChannelDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.NotificationChannelMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = NotificationChannelMode.class)
public class NotificationChannelRestDTOMapper extends RestSimpleDTOMapper<NotificationChannelEntity, NotificationChannelDTOv1> {
    @Override
    public void map(NotificationChannelEntity src, NotificationChannelDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setNotifierFeaturerId(src.getNotifierFeaturerId())
                .setNotifierParams(src.getNotifierParams());
    }
}
