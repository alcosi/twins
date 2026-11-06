package org.twins.core.mappers.rest.resource;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.ResourceSearch;
import org.twins.core.dto.rest.resource.ResourceSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class ResourceSearchRestDTOReverseMapper extends RestSimpleDTOMapper<ResourceSearchDTOv1, ResourceSearch> {

    @Override
    public void map(ResourceSearchDTOv1 src, ResourceSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setOriginalFileNameLikeList(src.getOriginalFileNameLikeList())
                .setUploadedByUserIdList(src.getUploadedByUserIdList());
    }

}
