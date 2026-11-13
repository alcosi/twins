package org.twins.core.mappers.rest.resource;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.ResourceSearch;
import org.twins.core.dto.rest.resource.ResourceSearchDTOv1;
import org.twins.core.mappers.rest.DataTimeRangeDTOReverseMapper;
import org.twins.core.mappers.rest.LongRangeDTOReverseMapper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class ResourceSearchRestDTOReverseMapper extends RestSimpleDTOMapper<ResourceSearchDTOv1, ResourceSearch> {
    private final DataTimeRangeDTOReverseMapper dataTimeRangeDTOReverseMapper;
    private final LongRangeDTOReverseMapper longRangeDTOReverseMapper;

    @Override
    public void map(ResourceSearchDTOv1 src, ResourceSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setOriginalFileNameLikeList(src.getOriginalFileNameLikeList())
                .setOriginalFileNameNotLikeList(src.getOriginalFileNameNotLikeList())
                .setUploadedByUserIdList(src.getUploadedByUserIdList())
                .setUploadedByUserIdExcludeList(src.getUploadedByUserIdExcludeList())
                .setStorageIdList(src.getStorageIdList())
                .setStorageIdExcludeList(src.getStorageIdExcludeList())
                .setStorageFileKeyLikeList(src.getStorageFileKeyLikeList())
                .setStorageFileKeyNotLikeList(src.getStorageFileKeyNotLikeList())
                .setSizeInBytes(longRangeDTOReverseMapper.convert(src.getSizeInBytes()))
                .setCreatedAt(dataTimeRangeDTOReverseMapper.convert(src.getCreatedAt()))
                .setIsPublic(src.getIsPublic());
    }

}
