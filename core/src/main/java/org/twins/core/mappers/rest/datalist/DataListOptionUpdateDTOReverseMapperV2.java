package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.datalist.DataListOptionUpdate;
import org.twins.core.dto.rest.datalist.DataListOptionUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class DataListOptionUpdateDTOReverseMapperV2 extends RestSimpleDTOMapper<DataListOptionUpdateDTOv1, DataListOptionUpdate> {
    private final DataListOptionSaveDTOReverseMapperV2 dataListOptionSaveDTOReverseMapper;

    @Override
    public void map(DataListOptionUpdateDTOv1 src, DataListOptionUpdate dst, MapperContext mapperContext) throws Exception {
        dataListOptionSaveDTOReverseMapper.map(src, dst, mapperContext);
        dst
                .setId(src.getId())
                .setStatus(src.getStatus());
    }
}