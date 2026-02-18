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

            twinClassRestDTOMapper.postpone(src.getTwinClass(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassMode.HistoryNotification2TwinClassMode.SHORT)));
        }

        if (mapperContext.hasModeButNot(TwinValidatorSetMode.HistoryNotification2TwinValidatorSetMode.HIDE)) {
            dst.setTwinValidatorSetId(src.getTwinValidatorSetId());

            twinValidatorSetRestDTOMapper.postpone(src.getTwinValidatorSet(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinValidatorSetMode.HistoryNotification2TwinValidatorSetMode.SHORT)));
        }

        if (mapperContext.hasModeButNot(HistoryNotificationRecipientMode.HistoryNotification2HistoryNotificationRecipientMode.HIDE)) {
            dst.setHistoryNotificationRecipientId(src.getHistoryNotificationRecipientId());

            historyNotificationRecipientDTOMapperV1.postpone(src.getHistoryNotificationRecipient(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(HistoryNotificationRecipientMode.HistoryNotification2HistoryNotificationRecipientMode.SHORT)));
        }
    }
}
