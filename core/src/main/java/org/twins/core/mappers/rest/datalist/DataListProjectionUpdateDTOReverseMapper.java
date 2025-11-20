package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListProjectionEntity;
import org.twins.core.dto.rest.datalist.DataListProjectionCreateDTOv1;
import org.twins.core.dto.rest.datalist.DataListProjectionUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class DataListProjectionUpdateDTOReverseMapper extends RestSimpleDTOMapper<DataListProjectionUpdateDTOv1, DataListProjectionEntity> {
    private final DataListProjectionSaveDTOReverseMapper dataListProjectionSaveDTOReverseMapper;

    @Override
    public void map(DataListProjectionUpdateDTOv1 src, DataListProjectionEntity dst, MapperContext mapperContext) throws Exception {
        dataListProjectionSaveDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }
}
