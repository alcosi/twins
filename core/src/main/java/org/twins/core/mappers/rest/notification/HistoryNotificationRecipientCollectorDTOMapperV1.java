package org.twins.core.mappers.rest.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.notification.HistoryNotificationRecipientCollectorEntity;
import org.twins.core.dto.rest.notification.HistoryNotificationRecipientCollectorDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.HistoryNotificationRecipientCollectorMode;

@RequiredArgsConstructor
@MapperModeBinding(modes = HistoryNotificationRecipientCollectorMode.class)
@Component
public class HistoryNotificationRecipientCollectorDTOMapperV1 extends RestSimpleDTOMapper<HistoryNotificationRecipientCollectorEntity, HistoryNotificationRecipientCollectorDTOv1> {
    private final HistoryNotificationRecipientDTOMapperV1 historyNotificationRecipientDTOMapperV1;

    @Override
    public void map(HistoryNotificationRecipientCollectorEntity src, HistoryNotificationRecipientCollectorDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(HistoryNotificationRecipientCollectorMode.DETAILED)) {
            case DETAILED ->
                    dst
                            .setId(src.getId())
                            .setRecipientId(src.getHistoryNotificationRecipientId())
                            .setRecipientResolverFeaturerId(src.getRecipientResolverFeaturerId())
                            .setRecipientResolverParams(src.getRecipientResolverParams())
                            .setExclude(src.getExclude());
            case SHORT ->
                    dst
                            .setId(src.getId())
                            .setRecipientId(src.getHistoryNotificationRecipientId())
                            .setExclude(src.getExclude());
        }

        if (mapperContext.hasModeButNot(HistoryNotificationRecipientCollectorMode.HistoryNotificationRecipient2HistoryNotificationRecipientCollectorMode.HIDE)) {
            dst.setRecipientId(src.getHistoryNotificationRecipientId());

            historyNotificationRecipientDTOMapperV1.postpone(src.getHistoryNotificationRecipientEntity(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(HistoryNotificationRecipientCollectorMode.HistoryNotificationRecipient2HistoryNotificationRecipientCollectorMode.SHORT)));
        }

        if (mapperContext.hasModeButNot(HistoryNotificationRecipientCollectorMode.HistoryNotificationRecipientResolverFeaturer2HistoryNotificationRecipientCollectorMode.HIDE)) {
            dst.setRecipientResolverFeaturerId(src.getRecipientResolverFeaturerId());

            historyNotificationRecipientDTOMapperV1.postpone(src.getHistoryNotificationRecipientEntity(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(HistoryNotificationRecipientCollectorMode.HistoryNotificationRecipientResolverFeaturer2HistoryNotificationRecipientCollectorMode.SHORT)));
        }
    }
}
