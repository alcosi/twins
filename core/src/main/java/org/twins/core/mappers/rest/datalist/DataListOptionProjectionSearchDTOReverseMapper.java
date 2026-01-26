package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.DataListOptionProjectionSearch;
import org.twins.core.dto.rest.datalist.DataListOptionProjectionSearchDTOv1;
import org.twins.core.mappers.rest.DataTimeRangeDTOReverseMapper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class DataListOptionProjectionSearchDTOReverseMapper extends RestSimpleDTOMapper<DataListOptionProjectionSearchDTOv1, DataListOptionProjectionSearch> {

    private final DataTimeRangeDTOReverseMapper dataTimeRangeDTOReverseMapper;

    @Override
    public void map(DataListOptionProjectionSearchDTOv1 src, DataListOptionProjectionSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setProjectionTypeIdList(src.getProjectionTypeIdList())
                .setProjectionTypeIdExcludeList(src.getProjectionTypeIdExcludeList())
                .setSrcDataListOptionIdList(src.getSrcDataListOptionIdList())
                .setSrcDataListOptionIdExcludeList(src.getSrcDataListOptionIdExcludeList())
                .setDstDataListOptionIdList(src.getDstDataListOptionIdList())
                .setDstDataListOptionIdExcludeList(src.getDstDataListOptionIdExcludeList())
                .setSavedByUserIdList(src.getSavedByUserIdList())
                .setSavedByUserIdExcludeList(src.getSavedByUserIdExcludeList())
                .setChangedAt(dataTimeRangeDTOReverseMapper.convert(src.getChangedAt()));
    }
}
