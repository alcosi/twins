package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.datalist.DataListOptionCreate;
import org.twins.core.dto.rest.datalist.DataListOptionCreateDTOv1;
import org.twins.core.dto.rest.datalist.DataListOptionCreateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Deprecated
@Component
@RequiredArgsConstructor
public class DataListOptionCreateDTOReverseMapper extends RestSimpleDTOMapper<DataListOptionCreateRqDTOv1, DataListOptionCreate> {
    private final DataListOptionSaveDTOReverseMapper dataListOptionSaveDTOReverseMapper;

    @Override
    public void map(DataListOptionCreateRqDTOv1 src, DataListOptionCreate dst, MapperContext mapperContext) throws Exception {
        dataListOptionSaveDTOReverseMapper.map(src, dst, mapperContext);
        dst
                .setDataListId(src.getDataListId());
    }
}
