package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dto.rest.datalist.DataListRsDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DataListMode;
import org.twins.core.mappers.rest.mappercontext.modes.DataListOptionMode;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.datalist.DataListService;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {DataListMode.class, DataListOptionMode.class})
public class DataListRsRestDTOMapperV2 extends RestSimpleDTOMapper<DataListEntity, DataListRsDTOv2> {
    private final DataListRestDTOMapper dataListRestDTOMapper;
    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;
    private final DataListService dataListService;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @Override
    public void map(DataListEntity src, DataListRsDTOv2 dst, MapperContext mapperContext) throws Exception {
        dst.setDataList(dataListRestDTOMapper.convert(src, mapperContext));
        if (mapperContext.hasModeButNot(DataListOptionMode.HIDE)) {
            dataListService.loadDataListOptions(src);
            dst.setOptions(src.getOptions().getIdSet());
            dataListOptionRestDTOMapper.postpone(src.getOptions(), mapperContext);
        }
        dst.setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
    }

    @Override
    public String getObjectCacheId(DataListEntity src) {
        return src.getId().toString();
    }
}
