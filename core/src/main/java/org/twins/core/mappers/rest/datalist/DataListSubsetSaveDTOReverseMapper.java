package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListSubsetEntity;
import org.twins.core.dto.rest.datalist.DataListSubsetSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class DataListSubsetSaveDTOReverseMapper extends RestSimpleDTOMapper<DataListSubsetSaveDTOv1, DataListSubsetEntity> {
    @Override
    public void map(DataListSubsetSaveDTOv1 src, DataListSubsetEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setDataListId(src.getDataListId())
                .setName(src.getName())
                .setDescription(src.getDescription())
                .setKey(src.getKey());
    }
}
