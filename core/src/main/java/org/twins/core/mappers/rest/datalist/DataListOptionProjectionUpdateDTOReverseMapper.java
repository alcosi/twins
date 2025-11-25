package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionProjectionEntity;
import org.twins.core.dto.rest.datalist.DataListOptionProjectionCreateDTOv1;
import org.twins.core.dto.rest.datalist.DataListOptionProjectionUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class DataListOptionProjectionUpdateDTOReverseMapper extends RestSimpleDTOMapper<DataListOptionProjectionUpdateDTOv1, DataListOptionProjectionEntity> {
   private final DataListOptionProjectionSaveDTOReverseMapper dataListOptionProjectionSaveDTOReverseMapper;

    @Override
    public void map(DataListOptionProjectionUpdateDTOv1 src, DataListOptionProjectionEntity dst, MapperContext mapperContext) throws Exception {
        dataListOptionProjectionSaveDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }
}
