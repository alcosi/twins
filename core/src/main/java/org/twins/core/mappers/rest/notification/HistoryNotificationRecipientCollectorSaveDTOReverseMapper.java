package org.twins.core.mappers.rest.notification;

import org.springframework.stereotype.Component;
import org.twins.core.domain.notification.HistoryNotificationRecipientCollectorSave;
import org.twins.core.dto.rest.notification.HistoryNotificationRecipientCollectorSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class HistoryNotificationRecipientCollectorSaveDTOReverseMapper extends RestSimpleDTOMapper<HistoryNotificationRecipientCollectorSaveDTOv1, HistoryNotificationRecipientCollectorSave> {
    @Override
    public void map(HistoryNotificationRecipientCollectorSaveDTOv1 src, HistoryNotificationRecipientCollectorSave dst, MapperContext mapperContext) throws Exception {
        dst
                .setRecipientId(src.getRecipientId())
                .setRecipientResolverFeaturerId(src.getRecipientResolverFeaturerId())
                .setRecipientResolverParams(src.getRecipientResolverParams())
                .setExclude(src.getExclude());
    }
}
