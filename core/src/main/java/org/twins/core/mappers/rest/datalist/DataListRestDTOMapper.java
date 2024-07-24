package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dto.rest.datalist.DataListDTOv1;
import org.twins.core.mappers.rest.mappercontext.modes.DataListMode;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.modes.DataListOptionMode;
import org.twins.core.service.datalist.DataListService;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DataListMode.class)
public class DataListRestDTOMapper extends RestSimpleDTOMapper<DataListEntity, DataListDTOv1> {

    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;

    private final DataListService dataListService;

    @Override
    public void map(DataListEntity src, DataListDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(DataListMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setName(src.getName())
                        .setUpdatedAt(src.getUpdatedAt().toLocalDateTime())
                        .setDescription(src.getDescription());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setName(src.getName());
                break;
        }
        if (mapperContext.hasModeButNot(DataListOptionMode.HIDE)) {
            dataListService.loadDataListOptions(src);
//            dst.setOptions(dataListOptionRestDTOMapper.convertMap(src.getOptions().getMap(), mapperContext)); //todo remove me after gateway support of relateMap of dataListOptions
            convertMapOrPostpone(src.getOptions(), dst, dataListOptionRestDTOMapper, mapperContext, DataListDTOv1::setOptions, DataListDTOv1::setOptionIdList);
        }
    }

    @Override
    public String getObjectCacheId(DataListEntity src) {
        return src.getId().toString();
    }
}
