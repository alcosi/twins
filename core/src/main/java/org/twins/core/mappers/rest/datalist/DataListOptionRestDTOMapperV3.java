package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv3;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.businessaccount.BusinessAccountDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.BusinessAccountMode;
import org.twins.core.mappers.rest.mappercontext.modes.DataListMode;


@Component
@RequiredArgsConstructor
public class DataListOptionRestDTOMapperV3 extends RestSimpleDTOMapper<DataListOptionEntity, DataListOptionDTOv3> {

    private final DataListOptionRestDTOMapperV2 dataListOptionRestDTOMapperV2;

    @MapperModePointerBinding(modes = DataListMode.DataListOption2DataListMode.class)
    private final DataListRestDTOMapper dataListRestDTOMapper;
    @MapperModePointerBinding(modes = BusinessAccountMode.DataListOption2BusinessAccountMode.class)
    private final BusinessAccountDTOMapper businessAccountDTOMapper;

    @Override
    public void map(DataListOptionEntity src, DataListOptionDTOv3 dst, MapperContext mapperContext) throws Exception {
        dataListOptionRestDTOMapperV2.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(DataListMode.DataListOption2DataListMode.HIDE))
            dst
                    .setDataList(dataListRestDTOMapper.convertOrPostpone(src.getDataList(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(DataListMode.DataListOption2DataListMode.SHORT))))
                    .setDataListId(src.getDataListId());
        if (mapperContext.hasModeButNot(BusinessAccountMode.DataListOption2BusinessAccountMode.HIDE))
            dst
                    .setBusinessAccount(businessAccountDTOMapper.convertOrPostpone(src.getBusinessAccount(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(BusinessAccountMode.DataListOption2BusinessAccountMode.SHORT))))
                    .setBusinessAccountId(src.getBusinessAccountId());
    }
}
