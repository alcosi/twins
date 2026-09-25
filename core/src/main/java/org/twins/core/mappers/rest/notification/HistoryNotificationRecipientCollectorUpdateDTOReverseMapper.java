package org.twins.core.mappers.rest.notification;

import org.springframework.stereotype.Component;
import org.twins.core.domain.notification.HistoryNotificationRecipientCollectorUpdate;
import org.twins.core.dto.rest.notification.HistoryNotificationRecipientCollectorUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class HistoryNotificationRecipientCollectorUpdateDTOReverseMapper extends RestSimpleDTOMapper<HistoryNotificationRecipientCollectorUpdateDTOv1, HistoryNotificationRecipientCollectorUpdate> {

    @Override
    public void map(HistoryNotificationRecipientCollectorUpdateDTOv1 src, HistoryNotificationRecipientCollectorUpdate dst, MapperContext mapperContext) throws Exception {
        dst.setId(src.getId());
        dst
                .setRecipientId(src.getRecipientId())
                .setRecipientResolverFeaturerId(src.getRecipientResolverFeaturerId())
                .setRecipientResolverParams(src.getRecipientResolverParams())
                .setExclude(src.getExclude());
    }
}
