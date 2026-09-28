package org.twins.core.mappers.rest.notification;

import org.springframework.stereotype.Component;
import org.twins.core.domain.notification.HistoryNotificationRecipientCollectorCreate;
import org.twins.core.dto.rest.notification.HistoryNotificationRecipientCollectorCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class HistoryNotificationRecipientCollectorCreateDTOReverseMapper extends RestSimpleDTOMapper<HistoryNotificationRecipientCollectorCreateDTOv1, HistoryNotificationRecipientCollectorCreate> {

    @Override
    public void map(HistoryNotificationRecipientCollectorCreateDTOv1 src, HistoryNotificationRecipientCollectorCreate dst, MapperContext mapperContext) throws Exception {
        dst
                .setRecipientId(src.getRecipientId())
                .setRecipientResolverFeaturerId(src.getRecipientResolverFeaturerId())
                .setRecipientResolverParams(src.getRecipientResolverParams())
                .setExclude(src.getExclude());
    }
}
