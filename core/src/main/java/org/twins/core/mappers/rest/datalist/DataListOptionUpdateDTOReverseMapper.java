package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.datalist.DataListOptionUpdate;
import org.twins.core.dto.rest.datalist.DataListOptionUpdateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class DataListOptionUpdateDTOReverseMapper extends RestSimpleDTOMapper<DataListOptionUpdateRqDTOv1, DataListOptionUpdate> {

    private final DataListOptionSaveDTOReverseMapper dataListOptionSaveDTOReverseMapper;

    @Override
    public void map(DataListOptionUpdateRqDTOv1 src, DataListOptionUpdate dst, MapperContext mapperContext) throws Exception {
        dataListOptionSaveDTOReverseMapper.map(src, dst, mapperContext);
        dst
                .setStatus(src.getStatus());
    }
}
