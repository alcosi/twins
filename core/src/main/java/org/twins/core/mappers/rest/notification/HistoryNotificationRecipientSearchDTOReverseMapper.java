package org.twins.core.mappers.rest.notification;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.HistoryNotificationRecipientSearch;
import org.twins.core.dto.rest.notification.HistoryNotificationRecipientSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class HistoryNotificationRecipientSearchDTOReverseMapper extends RestSimpleDTOMapper<HistoryNotificationRecipientSearchDTOv1, HistoryNotificationRecipientSearch> {
    @Override
    public void map(HistoryNotificationRecipientSearchDTOv1 src, HistoryNotificationRecipientSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setNameLikeList(src.getNameLikeList())
                .setNameNotLikeList(src.getNameNotLikeList())
                .setDescriptionLikeList(src.getDescriptionLikeList())
                .setDescriptionNotLikeList(src.getDescriptionNotLikeList());
    }
}
