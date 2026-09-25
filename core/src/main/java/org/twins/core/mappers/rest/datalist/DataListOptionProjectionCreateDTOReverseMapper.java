package org.twins.core.mappers.rest.datalist;

import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionProjectionEntity;
import org.twins.core.dto.rest.datalist.DataListOptionProjectionCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class DataListOptionProjectionCreateDTOReverseMapper extends RestSimpleDTOMapper<DataListOptionProjectionCreateDTOv1, DataListOptionProjectionEntity> {

    @Override
    public void map(DataListOptionProjectionCreateDTOv1 src, DataListOptionProjectionEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setProjectionTypeId(src.getProjectionTypeId())
                .setSrcDataListOptionId(src.getSrcDataListOptionId())
                .setDstDataListOptionId(src.getDstDataListOptionId());
    }
}
