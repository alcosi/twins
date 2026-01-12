package org.twins.core.mappers.rest.notification;

import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.notification.HistoryNotificationRecipientCollectorEntity;
import org.twins.core.dto.rest.notification.HistoryNotificationRecipientCollectorDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.HistoryNotificationRecipientCollectorMode;

@MapperModeBinding(modes = HistoryNotificationRecipientCollectorMode.class)
@Component
public class HistoryNotificationRecipientCollectorDTOMapperV1 extends RestSimpleDTOMapper<HistoryNotificationRecipientCollectorEntity, HistoryNotificationRecipientCollectorDTOv1> {
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
    }
}
