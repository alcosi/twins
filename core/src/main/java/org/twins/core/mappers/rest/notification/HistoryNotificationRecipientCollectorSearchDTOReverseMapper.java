package org.twins.core.mappers.rest.notification;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.HistoryNotificationRecipientCollectorSearch;
import org.twins.core.dto.rest.notification.HistoryNotificationRecipientCollectorSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class HistoryNotificationRecipientCollectorSearchDTOReverseMapper extends RestSimpleDTOMapper<HistoryNotificationRecipientCollectorSearchDTOv1, HistoryNotificationRecipientCollectorSearch> {
    @Override
    public void map(HistoryNotificationRecipientCollectorSearchDTOv1 src, HistoryNotificationRecipientCollectorSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setRecipientIdList(src.getRecipientIdList())
                .setRecipientIdExcludeList(src.getRecipientIdExcludeList())
                .setRecipientResolverFeaturerIdList(src.getRecipientResolverFeaturerIdList())
                .setExclude(src.getExclude());
    }
}
