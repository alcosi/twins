package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TierSearch;
import org.twins.core.dto.rest.domain.TierSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TierSearchDTOReverseMapper extends RestSimpleDTOMapper<TierSearchRqDTOv1, TierSearch> {
    public void map(TierSearchRqDTOv1 src, TierSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setPermissionSchemaIdList(src.getPermissionSchemaIdList())
                .setPermissionSchemaIdExcludeList(src.getPermissionSchemaIdExcludeList())
                .setTwinflowSchemaIdList(src.getTwinflowSchemaIdList())
                .setTwinflowSchemaIdExcludeList(src.getTwinflowSchemaIdExcludeList())
                .setTwinclassSchemaIdList(src.getTwinclassSchemaIdList())
                .setTwinclassSchemaIdExcludeList(src.getTwinclassSchemaIdExcludeList())
                .setNameLikeList(src.getNameLikeList())
                .setNameNotLikeList(src.getNameNotLikeList())
                .setDescriptionLikeList(src.getDescriptionLikeList())
                .setDescriptionNotLikeList(src.getDescriptionNotLikeList())
                .setAttachmentsStorageQuotaCountRange(src.getAttachmentsStorageQuotaCountRange())
                .setAttachmentsStorageQuotaSizeRange(src.getAttachmentsStorageQuotaSizeRange())
                .setUserCountQuotaRange(src.getUserCountQuotaRange())
                .setCustom(src.getCustom());
    }
}
