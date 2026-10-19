package org.twins.core.mappers.rest.datalist;

import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionProjectionEntity;
import org.twins.core.dto.rest.datalist.DataListOptionProjectionUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class DataListOptionProjectionUpdateDTOReverseMapper extends RestSimpleDTOMapper<DataListOptionProjectionUpdateDTOv1, DataListOptionProjectionEntity> {

    @Override
    public void map(DataListOptionProjectionUpdateDTOv1 src, DataListOptionProjectionEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setProjectionTypeId(src.getProjectionTypeId())
                .setSrcDataListOptionId(src.getSrcDataListOptionId())
                .setDstDataListOptionId(src.getDstDataListOptionId());
    }
}
