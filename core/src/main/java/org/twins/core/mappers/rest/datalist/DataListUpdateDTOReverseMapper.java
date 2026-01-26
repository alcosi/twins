package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.datalist.DataListUpdate;
import org.twins.core.dto.rest.datalist.DataListUpdateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class DataListUpdateDTOReverseMapper extends RestSimpleDTOMapper<DataListUpdateRqDTOv1, DataListUpdate> {

    private final DataListSaveDTOReverseMapper dataListSaveDTOReverseMapper;

    @Override
    public void map(DataListUpdateRqDTOv1 src, DataListUpdate dst, MapperContext mapperContext) throws Exception {
        dataListSaveDTOReverseMapper.map(src, dst, mapperContext);
        dst
                .setDefaultOptionId(src.getDefaultOptionId());
    }
}
