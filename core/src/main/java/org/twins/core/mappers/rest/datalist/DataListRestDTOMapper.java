package org.twins.core.mappers.rest.datalist;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dto.rest.datalist.DataListDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.service.datalist.DataListService;

import java.util.UUID;


@Component
@RequiredArgsConstructor
public class DataListRestDTOMapper extends RestSimpleDTOMapper<DataListEntity, DataListDTOv1> {
    private final DataListService dataListService;
    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;

    @Override
    public void map(DataListEntity src, DataListDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(DataListRestDTOMapper.Mode.DETAILED)) {
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
        if (!dataListOptionRestDTOMapper.hideMode(mapperContext)) {
            Kit<DataListOptionEntity, UUID> options = dataListService.findDataListOptions(src, mapperContext.getModePagination(DataListOptionRestDTOMapper.Mode.class));
            dst.setOptions(dataListOptionRestDTOMapper.convertMap(options.getMap(), mapperContext)); //todo remove me after gateway support of relateMap of dataListOptions
            convertMapOrPostpone(options, dst, dataListOptionRestDTOMapper, mapperContext, DataListDTOv1::setOptions, DataListDTOv1::setOptionIdList);
        }
    }

    @AllArgsConstructor
    public enum Mode implements MapperMode {
        HIDE(0),
        SHORT(1),
        DETAILED(2);

        public static final String _HIDE = "HIDE";
        public static final String _SHORT = "SHORT";
        public static final String _DETAILED = "DETAILED";

        @Getter
        final int priority;
    }

    @Override
    public String getObjectCacheId(DataListEntity src) {
        return src.getId().toString();
    }
}
