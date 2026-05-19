package org.twins.core.mappers.rest.notification;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.NotificationSchemaSearch;
import org.twins.core.dto.rest.notification.NotificationSchemaSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class NotificationSchemaSearchDTOReverseMapper extends RestSimpleDTOMapper<NotificationSchemaSearchDTOv1, NotificationSchemaSearch> {

    @Override
    public void map(NotificationSchemaSearchDTOv1 src, NotificationSchemaSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setNameLikeList(src.getNameLikeList())
                .setNameNotLikeList(src.getNameNotLikeList())
                .setCreatedByUserIdList(src.getCreatedByUserIdList())
                .setCreatedByUserIdExcludeList(src.getCreatedByUserIdExcludeList());
    }
}
