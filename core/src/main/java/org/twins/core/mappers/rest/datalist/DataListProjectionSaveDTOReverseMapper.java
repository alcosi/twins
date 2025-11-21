package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListProjectionEntity;
import org.twins.core.dto.rest.datalist.DataListProjectionSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class DataListProjectionSaveDTOReverseMapper extends RestSimpleDTOMapper<DataListProjectionSaveDTOv1, DataListProjectionEntity> {
    @Override
    public void map(DataListProjectionSaveDTOv1 src, DataListProjectionEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setSrcDataListId(src.getSrcDataListId())
                .setDstDataListId(src.getDstDataListId())
                .setName(src.getName());
    }
}
