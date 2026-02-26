package org.twins.core.mappers.rest.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.notification.HistoryNotificationEntity;
import org.twins.core.dto.rest.notification.HistoryNotificationDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.validator.TwinValidatorSetRestDTOMapper;
import org.twins.core.service.notification.HistoryNotificationService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {HistoryNotificationMode.class})
public class HistoryNotificationDTOMapperV1 extends RestSimpleDTOMapper<HistoryNotificationEntity, HistoryNotificationDTOv1> {
    @MapperModePointerBinding(modes = TwinClassMode.HistoryNotification2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @MapperModePointerBinding(modes = TwinValidatorSetMode.HistoryNotification2TwinValidatorSetMode.class)
    private final TwinValidatorSetRestDTOMapper twinValidatorSetRestDTOMapper;

    @MapperModePointerBinding(modes = HistoryNotificationRecipientMode.HistoryNotification2HistoryNotificationRecipientMode.class)
    private final HistoryNotificationRecipientDTOMapperV1 historyNotificationRecipientDTOMapperV1;

    @MapperModePointerBinding(modes = NotificationSchemaMode.HistoryNotification2NotificationSchemaMode.class)
    private final NotificationSchemaRestDTOMapper notificationSchemaRestDTOMapper;

    @MapperModePointerBinding(modes = NotificationChannelEventMode.HistoryNotification2NotificationChannelEventMode.class)
    private final NotificationChannelEventRestDTOMapper notificationChannelEventRestDTOMapper;

    private final HistoryNotificationService historyNotificationService;

    @Override
    public void map(HistoryNotificationEntity src, HistoryNotificationDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(HistoryNotificationMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setHistoryTypeId(src.getHistoryTypeId())
                        .setTwinClassId(src.getTwinClassId())
                        .setTwinClassFieldId(src.getTwinClassFieldId())
                        .setTwinValidatorSetId(src.getTwinValidatorSetId())
                        .setTwinValidatorSetInvert(src.getTwinValidatorSetInvert())
                        .setNotificationSchemaId(src.getNotificationSchemaId())
                        .setHistoryNotificationRecipientId(src.getHistoryNotificationRecipientId())
                        .setNotificationChannelEventId(src.getNotificationChannelEventId())
                        .setCreatedByUserId(src.getCreatedByUserId())
                        .setCreatedAt(src.getCreatedAt());
                break;
            case SHORT:
                dst
                        .setId(src.getId());
                break;
        }

        if (mapperContext.hasModeButNot(TwinClassMode.HistoryNotification2TwinClassMode.HIDE)) {
            dst.setTwinClassId(src.getTwinClassId());

            historyNotificationService.loadTwinClass(src);
            twinClassRestDTOMapper.postpone(src.getTwinClass(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassMode.HistoryNotification2TwinClassMode.SHORT)));
        }

        if (mapperContext.hasModeButNot(TwinValidatorSetMode.HistoryNotification2TwinValidatorSetMode.HIDE)) {
            dst.setTwinValidatorSetId(src.getTwinValidatorSetId());

            historyNotificationService.loadTwinValidatorSet(src);
            twinValidatorSetRestDTOMapper.postpone(src.getTwinValidatorSet(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinValidatorSetMode.HistoryNotification2TwinValidatorSetMode.SHORT)));
        }

        if (mapperContext.hasModeButNot(HistoryNotificationRecipientMode.HistoryNotification2HistoryNotificationRecipientMode.HIDE)) {
            dst.setHistoryNotificationRecipientId(src.getHistoryNotificationRecipientId());

            historyNotificationRecipientDTOMapperV1.postpone(src.getHistoryNotificationRecipient(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(HistoryNotificationRecipientMode.HistoryNotification2HistoryNotificationRecipientMode.SHORT)));
        }

        if (mapperContext.hasModeButNot(NotificationSchemaMode.HistoryNotification2NotificationSchemaMode.HIDE)) {
            dst.setNotificationSchemaId(src.getNotificationSchemaId());

            historyNotificationService.loadNotificationSchema(src);
            notificationSchemaRestDTOMapper.postpone(src.getNotificationSchema(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(NotificationSchemaMode.HistoryNotification2NotificationSchemaMode.SHORT)));
        }

        if (mapperContext.hasModeButNot(NotificationChannelEventMode.HistoryNotification2NotificationChannelEventMode.HIDE)) {
            dst.setNotificationChannelEventId(src.getNotificationChannelEventId());

            historyNotificationService.loadNotificationChannelEvent(src);
            notificationChannelEventRestDTOMapper.postpone(src.getNotificationChannelEvent(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(NotificationChannelEventMode.HistoryNotification2NotificationChannelEventMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<HistoryNotificationEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(TwinClassMode.HistoryNotification2TwinClassMode.HIDE)) {
            historyNotificationService.loadTwinClass(srcCollection);
        }
        if (mapperContext.hasModeButNot(TwinValidatorSetMode.HistoryNotification2TwinValidatorSetMode.HIDE)) {
            historyNotificationService.loadTwinValidatorSet(srcCollection);
        }
        if (mapperContext.hasModeButNot(NotificationSchemaMode.HistoryNotification2NotificationSchemaMode.HIDE)) {
            historyNotificationService.loadNotificationSchema(srcCollection);
        }
        if (mapperContext.hasModeButNot(NotificationChannelEventMode.HistoryNotification2NotificationChannelEventMode.HIDE)) {
            historyNotificationService.loadNotificationChannelEvent(srcCollection);
        }
    }
}
