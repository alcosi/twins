package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.datalist.DataListOptionCountDTOv1;
import org.twins.core.enums.sort.DataListOptionGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.businessaccount.BusinessAccountDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.BusinessAccountMode;
import org.twins.core.mappers.rest.mappercontext.modes.DataListMode;
import org.twins.core.mappers.rest.mappercontext.modes.DataListOptionMode;
import org.twins.core.service.datalist.DataListOptionService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DataListOptionMode.class)
public class DataListOptionCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<DataListOptionEntity, DataListOptionGroupField>, DataListOptionCountDTOv1> {

    @MapperModePointerBinding(modes = DataListMode.DataListOption2DataListMode.class)
    private final DataListRestDTOMapper dataListRestDTOMapper;

    @MapperModePointerBinding(modes = BusinessAccountMode.DataListOption2BusinessAccountMode.class)
    private final BusinessAccountDTOMapper businessAccountDTOMapper;

    private final DataListOptionService dataListOptionService;

    @Override
    public void map(CountResult<DataListOptionEntity, DataListOptionGroupField> src, DataListOptionCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setDataListId(entity.getDataListId())
                .setBusinessAccountId(entity.getBusinessAccountId())
                .setStatus(entity.getStatus())
                .setCustom(entity.isCustom())
                .setCount(src.getCount());
        if (needLoad(mapperContext, DataListMode.DataListOption2DataListMode.HIDE, src, DataListOptionGroupField.dataListId)) {
            dataListOptionService.loadDataList(entity);
            dataListRestDTOMapper.convertOrPostpone(entity.getDataList(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(DataListMode.DataListOption2DataListMode.SHORT)));
        }
        if (needLoad(mapperContext, BusinessAccountMode.DataListOption2BusinessAccountMode.HIDE, src, DataListOptionGroupField.businessAccountId)) {
            dataListOptionService.loadBusinessAccount(entity);
            businessAccountDTOMapper.convertOrPostpone(entity.getBusinessAccount(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(BusinessAccountMode.DataListOption2BusinessAccountMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<DataListOptionEntity, DataListOptionGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        if (srcCollection.isEmpty()) {
            return;
        }
        var entityCollection = srcCollection.stream().map(CountResult::getEntity).filter(java.util.Objects::nonNull).toList();
        if (entityCollection.isEmpty()) {
            return;
        }
        var sample = srcCollection.iterator().next();
        if (needLoad(mapperContext, DataListMode.DataListOption2DataListMode.HIDE, sample, DataListOptionGroupField.dataListId)) {
            dataListOptionService.loadDataList(entityCollection);
        }
        if (needLoad(mapperContext, BusinessAccountMode.DataListOption2BusinessAccountMode.HIDE, sample, DataListOptionGroupField.businessAccountId)) {
            dataListOptionService.loadBusinessAccount(entityCollection);
        }
    }
}
