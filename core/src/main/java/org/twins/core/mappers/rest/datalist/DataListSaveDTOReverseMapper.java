package org.twins.core.mappers.rest.datalist;

import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dto.rest.datalist.DataListSaveRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class DataListSaveDTOReverseMapper extends RestSimpleDTOMapper<DataListSaveRqDTOv1, DataListEntity> {

    @Override
    public void map(DataListSaveRqDTOv1 src, DataListEntity dst, MapperContext mapperContext) {
        dst
                .setKey(src.getKey())
                .setAttribute1key(src.getAttribute1() != null ?  src.getAttribute1().getKey() : null)
                .setAttribute2key(src.getAttribute2() != null ?  src.getAttribute2().getKey() : null)
                .setAttribute3key(src.getAttribute3() != null ?  src.getAttribute3().getKey() : null)
                .setAttribute4key(src.getAttribute4() != null ?  src.getAttribute4().getKey() : null);
    }
}
