package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dto.rest.datalist.DataListDTOv1;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.service.datalist.DataListService;


@Component
@RequiredArgsConstructor
public class DataListRestDTOMapper extends RestSimpleDTOMapper<DataListEntity, DataListDTOv1> {
    private final DataListService dataListService;
    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;

    @Override
    public void map(DataListEntity src, DataListDTOv1 dst, MapperProperties mapperProperties) throws Exception {
        switch (mapperProperties.getModeOrUse(DataListRestDTOMapper.Mode.DETAILED)) {
            case SHOW_OPTIONS:
                if (src.getOptions() == null)
                    src.setOptions(dataListService.findDataListOptions(src.getId()));
                dst.options(
                        dataListOptionRestDTOMapper.convertList(
                                src.getOptions(), mapperProperties));
            case DETAILED:
                dst
                        .id(src.getId())
                        .name(src.getName())
                        .updatedAt(src.getUpdatedAt().toInstant())
                        .description(src.getDescription());
            case ID_ONLY:
                dst
                        .id(src.getId());
                break;
            default:
                dst
                        .id(src.getId())
                        .name(src.getName());
        }

    }

    public enum Mode implements MapperMode {
        ID_ONLY, DETAILED, SHOW_OPTIONS;

        public static final String _ID_ONLY = "ID_ONLY";
        public static final String _DETAILED = "DETAILED";
        public static final String _SHOW_OPTIONS = "SHOW_OPTIONS";
    }
}
