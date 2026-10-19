package org.twins.core.mappers.rest.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.notification.NotificationChannelEventEntity;
import org.twins.core.dto.rest.notification.NotificationChannelEventDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.NotificationChannelEventMode;
import org.twins.core.mappers.rest.mappercontext.modes.NotificationChannelMode;
import org.twins.core.mappers.rest.mappercontext.modes.NotificationContextMode;
import org.twins.core.service.notification.NotificationChannelEventService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = NotificationChannelEventMode.class)
public class NotificationChannelEventRestDTOMapper extends RestSimpleDTOMapper<NotificationChannelEventEntity, NotificationChannelEventDTOv1> {
    @MapperModePointerBinding(modes = NotificationChannelMode.NotificationChannelEvent2NotificationChannelMode.class)
    private final NotificationChannelRestDTOMapper notificationChannelRestDTOMapper;

    @MapperModePointerBinding(modes = NotificationContextMode.NotificationChannelEvent2NotificationContextMode.class)
    private final NotificationContextRestDTOMapper notificationContextRestDTOMapper;

    private final NotificationChannelEventService notificationChannelEventService;

    @Override
    public void map(NotificationChannelEventEntity src, NotificationChannelEventDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(NotificationChannelEventMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setNotificationChannelId(src.getNotificationChannelId())
                        .setEventCode(src.getEventCode())
                        .setNotificationContextId(src.getNotificationContextId())
                        .setUniqueInBatch(src.isUniqueInBatch());
                break;
            case SHORT:
                dst
                        .setId(src.getId());
                break;
        }

        if (mapperContext.hasModeButNot(NotificationChannelMode.NotificationChannelEvent2NotificationChannelMode.HIDE)) {
            dst.setNotificationChannelId(src.getNotificationChannelId());

            notificationChannelEventService.loadNotificationChannel(src);
            notificationChannelRestDTOMapper.postpone(src.getNotificationChannel(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(NotificationChannelMode.NotificationChannelEvent2NotificationChannelMode.SHORT)));
        }

        if (mapperContext.hasModeButNot(NotificationContextMode.NotificationChannelEvent2NotificationContextMode.HIDE)) {
            dst.setNotificationContextId(src.getNotificationContextId());

            notificationChannelEventService.loadNotificationContext(src);
            notificationContextRestDTOMapper.postpone(src.getNotificationContext(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(NotificationContextMode.NotificationChannelEvent2NotificationContextMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<NotificationChannelEventEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(NotificationChannelMode.NotificationChannelEvent2NotificationChannelMode.HIDE)) {
            notificationChannelEventService.loadNotificationChannel(srcCollection);
        }
        if (mapperContext.hasModeButNot(NotificationContextMode.NotificationChannelEvent2NotificationContextMode.HIDE)) {
            notificationChannelEventService.loadNotificationContext(srcCollection);
        }
    }
}
