package org.twins.core.mappers.rest.datalist;

import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.datalist.DataListCountDTOv1;
import org.twins.core.enums.sort.DataListGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DataListMode;

import java.util.Collection;

@Component
@MapperModeBinding(modes = DataListMode.class)
public class DataListCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<DataListEntity, DataListGroupField>, DataListCountDTOv1> {

    @Override
    public void map(CountResult<DataListEntity, DataListGroupField> src, DataListCountDTOv1 dst, MapperContext mapperContext) {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setDefaultOptionId(entity.getDefaultDataListOptionId())
                .setCount(src.getCount());
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<DataListEntity, DataListGroupField>> srcCollection, MapperContext mapperContext) {
        // defaultOptionId is a plain scalar — no related object to load (DataListEntity has no @ManyToOne to the default DataListOption)
    }
}
