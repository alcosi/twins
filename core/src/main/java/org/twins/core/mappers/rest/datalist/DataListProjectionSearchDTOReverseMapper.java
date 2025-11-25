package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.DataListProjectionSearch;
import org.twins.core.domain.search.DataListSearch;
import org.twins.core.dto.rest.datalist.DataListProjectionSearchDTOv1;
import org.twins.core.dto.rest.datalist.DataListSearchRqDTOv1;
import org.twins.core.mappers.rest.DataTimeRangeDTOReverseMapper;
import org.twins.core.mappers.rest.LongRangeDTOReverseMapper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class DataListProjectionSearchDTOReverseMapper extends RestSimpleDTOMapper<DataListProjectionSearchDTOv1, DataListProjectionSearch> {

    private final DataTimeRangeDTOReverseMapper dataTimeRangeDTOReverseMapper;

    @Override
    public void map(DataListProjectionSearchDTOv1 src, DataListProjectionSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setNameLikeList(src.getNameLikeList())
                .setNameNotLikeList(src.getNameNotLikeList())
                .setSrcDataListIdList(src.getSrcDataListIdList())
                .setSrcDataListIdExcludeList(src.getSrcDataListIdExcludeList())
                .setDstDataListIdList(src.getDstDataListIdList())
                .setDstDataListIdExcludeList(src.getDstDataListIdExcludeList())
                .setSavedByUserIdList(src.getSavedByUserIdList())
                .setSavedByUserIdExcludeList(src.getSavedByUserIdExcludeList())
                .setChangedAt(dataTimeRangeDTOReverseMapper.convert(src.getChangedAt()));
    }
}
