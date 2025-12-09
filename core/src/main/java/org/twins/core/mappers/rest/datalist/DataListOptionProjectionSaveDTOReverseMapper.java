package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionProjectionEntity;
import org.twins.core.dto.rest.datalist.DataListOptionProjectionSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class DataListOptionProjectionSaveDTOReverseMapper extends RestSimpleDTOMapper<DataListOptionProjectionSaveDTOv1, DataListOptionProjectionEntity> {
    @Override
    public void map(DataListOptionProjectionSaveDTOv1 src, DataListOptionProjectionEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setProjectionTypeId(src.getProjectionTypeId())
                .setSrcDataListOptionId(src.getSrcDataListOptionId())
                .setDstDataListOptionId(src.getDstDataListOptionId());
    }
}
