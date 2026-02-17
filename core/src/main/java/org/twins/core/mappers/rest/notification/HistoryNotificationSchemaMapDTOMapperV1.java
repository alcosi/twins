package org.twins.core.mappers.rest.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.notification.HistoryNotificationSchemaMapEntity;
import org.twins.core.dto.rest.notification.HistoryNotificationSchemaMapDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.validator.TwinValidatorSetBaseV1RestDTOMapper;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {HistoryNotificationSchemaMapMode.class})
public class HistoryNotificationSchemaMapDTOMapperV1 extends RestSimpleDTOMapper<HistoryNotificationSchemaMapEntity, HistoryNotificationSchemaMapDTOv1> {
    @MapperModePointerBinding(modes = TwinClassMode.HistoryNotificationSchemaMap2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @MapperModePointerBinding(modes = TwinValidatorSetMode.HistoryNotificationSchemaMap2TwinValidatorSetMode.class)
    private final TwinValidatorSetBaseV1RestDTOMapper twinValidatorSetRestDTOMapper;

    @MapperModePointerBinding(modes = HistoryNotificationRecipientMode.HistoryNotificationSchemaMap2HistoryNotificationRecipientMode.class)
    private final HistoryNotificationRecipientDTOMapperV1 historyNotificationRecipientDTOMapperV1;

    @Override
    public void map(HistoryNotificationSchemaMapEntity src, HistoryNotificationSchemaMapDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(HistoryNotificationSchemaMapMode.DETAILED)) {
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
                        .setNotificationChannelEventId(src.getNotificationChannelEventId());
                break;
            case SHORT:
                dst
                        .setId(src.getId());
                break;
        }

        if (mapperContext.hasModeButNot(TwinClassMode.HistoryNotificationSchemaMap2TwinClassMode.HIDE)) {
            dst.setTwinClassId(src.getTwinClassId());

            twinClassRestDTOMapper.postpone(src.getTwinClass(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassMode.HistoryNotificationSchemaMap2TwinClassMode.SHORT)));
        }

        if (mapperContext.hasModeButNot(TwinValidatorSetMode.HistoryNotificationSchemaMap2TwinValidatorSetMode.HIDE)) {
            dst.setTwinValidatorSetId(src.getTwinValidatorSetId());

            twinValidatorSetRestDTOMapper.postpone(src.getTwinValidatorSet(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinValidatorSetMode.HistoryNotificationSchemaMap2TwinValidatorSetMode.SHORT)));
        }

        if (mapperContext.hasModeButNot(HistoryNotificationRecipientMode.HistoryNotificationSchemaMap2HistoryNotificationRecipientMode.HIDE)) {
            dst.setHistoryNotificationRecipientId(src.getHistoryNotificationRecipientId());

            historyNotificationRecipientDTOMapperV1.postpone(src.getHistoryNotificationRecipient(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(HistoryNotificationRecipientMode.HistoryNotificationSchemaMap2HistoryNotificationRecipientMode.SHORT)));
        }
    }
}
