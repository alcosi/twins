package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.datalist.DataListSubsetUpdate;
import org.twins.core.dto.rest.datalist.DataListSubsetUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class DataListSubsetUpdateDTOReverseMapper extends RestSimpleDTOMapper<DataListSubsetUpdateDTOv1, DataListSubsetUpdate> {
    private final DataListSubsetSaveDTOReverseMapper dataListSubsetSaveDTOReverseMapper;

    @Override
    public void map(DataListSubsetUpdateDTOv1 src, DataListSubsetUpdate dst, MapperContext mapperContext) throws Exception {
        dataListSubsetSaveDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }
}
