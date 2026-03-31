package org.twins.core.mappers.rest.twinclass;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinClassFreezeSearch;
import org.twins.core.dto.rest.twinclass.TwinClassFreezeSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TwinClassFreezeSearchRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFreezeSearchDTOv1, TwinClassFreezeSearch> {

    @Override
    public void map(TwinClassFreezeSearchDTOv1 src, TwinClassFreezeSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setKeyLikeList(src.getKeyLikeList())
                .setKeyNotLikeList(src.getKeyNotLikeList())
                .setStatusIdList(src.getStatusIdList())
                .setStatusIdExcludeList(src.getStatusIdExcludeList())
                .setNameLikeList(src.getNameLikeList())
                .setNameNotLikeList(src.getNameNotLikeList())
                .setDescriptionLikeList(src.getDescriptionLikeList())
                .setDescriptionNotLikeList(src.getDescriptionNotLikeList());
    }
}
