package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.datalist.DataListSubsetCreate;
import org.twins.core.dto.rest.datalist.DataListSubsetCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class DataListSubsetCreateDTOReverseMapper extends RestSimpleDTOMapper<DataListSubsetCreateDTOv1, DataListSubsetCreate> {
    private final DataListSubsetSaveDTOReverseMapper dataListSubsetSaveDTOReverseMapper;

    @Override
    public void map(DataListSubsetCreateDTOv1 src, DataListSubsetCreate dst, MapperContext mapperContext) throws Exception {
        dataListSubsetSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}
