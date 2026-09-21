package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.DataListSubsetSearch;
import org.twins.core.dto.rest.datalist.DataListSubsetSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class DataListSubsetSearchDTOReverseMapper extends RestSimpleDTOMapper<DataListSubsetSearchDTOv1, DataListSubsetSearch> {

    @Override
    public void map(DataListSubsetSearchDTOv1 src, DataListSubsetSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setDataListIdList(src.getDataListIdList())
                .setDataListIdExcludeList(src.getDataListIdExcludeList())
                .setNameLikeList(src.getNameLikeList())
                .setNameNotLikeList(src.getNameNotLikeList())
                .setKeyLikeList(src.getKeyLikeList())
                .setKeyNotLikeList(src.getKeyNotLikeList());
    }
}
