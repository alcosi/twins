package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dto.rest.datalist.DataListDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.service.datalist.DataListService;


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
                        .id(src.getId())
                        .name(src.getName())
                        .updatedAt(src.getUpdatedAt().toInstant())
                        .description(src.getDescription());
                break;
            case SHORT:
                dst
                        .id(src.getId())
                        .name(src.getName());
                break;
        }
        if (!dataListOptionRestDTOMapper.hideMode(mapperContext)) {
            dataListService.loadDataListOptions(src);
            dst.options(dataListOptionRestDTOMapper.convertMap(src.getOptions(), mapperContext));
        }
    }

    public enum Mode implements MapperMode {
        SHORT, DETAILED;

        public static final String _SHORT = "SHORT";
        public static final String _DETAILED = "DETAILED";
    }

    @Override
    public String getObjectCacheId(DataListEntity src) {
        return src.getId().toString();
    }
}
