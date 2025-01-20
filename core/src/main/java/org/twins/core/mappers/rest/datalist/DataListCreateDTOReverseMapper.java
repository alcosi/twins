package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.datalist.DataListSave;
import org.twins.core.dto.rest.datalist.DataListCreateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class DataListCreateDTOReverseMapper extends RestSimpleDTOMapper<DataListCreateRqDTOv1, DataListSave> {

    private final DataListSaveDTOReverseMapper dataListSaveDTOReverseMapper;

    @Override
    public void map(DataListCreateRqDTOv1 src, DataListSave dst, MapperContext mapperContext) throws Exception {
        dataListSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}
