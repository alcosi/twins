package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dto.rest.twin.TwinDTOv1;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.user.UserDTOMapper;
import org.twins.core.service.twin.TwinService;

import java.util.List;


@Component
@RequiredArgsConstructor
public class TwinRestDTOMapper extends RestSimpleDTOMapper<TwinEntity, TwinDTOv1> {
    final UserDTOMapper userDTOMapper;
    final TwinStatusRestDTOMapper twinStatusRestDTOMapper;
    final TwinClassRestDTOMapper twinClassRestDTOMapper;
    final TwinFieldRestDTOMapper twinFieldRestDTOMapper;
    final TwinService twinService;

    @Override
    public void map(TwinEntity src, TwinDTOv1 dst, MapperProperties mapperProperties) throws Exception {
        switch (mapperProperties.getModeOrUse(TwinMode.ID_NAME_ONLY)) {
            case NONE:
                return;
            case DETAILED:
                dst
                        .description(src.description())
                        .assignerUser(userDTOMapper.convert(src.assignerUser(), mapperProperties))
                        .authorUser(userDTOMapper.convert(src.createdByUser(), mapperProperties))
                        .status(twinStatusRestDTOMapper.convert(src.twinStatus(), mapperProperties))
                        .twinClass(twinClassRestDTOMapper.convert(src.twinClass(), mapperProperties))
                        .createdAt(src.createdAt().toInstant());
            case ID_NAME_ONLY:
                dst
                        .id(src.id())
                        .name(src.name());
        }

        List<TwinFieldEntity> twinFieldEntityList;
        switch (mapperProperties.getModeOrUse(FieldsMode.ALL_FIELDS)) {
            case NO_FIELDS:
                return;
            case ALL_FIELDS:
                twinFieldEntityList = twinService.findTwinFieldsIncludeMissing(src);
                dst.fields(twinFieldRestDTOMapper.convertList(twinFieldEntityList, mapperProperties));
                return;
            case NOT_EMPTY_FIELDS:
                twinFieldEntityList = twinService.findTwinFields(src.id());
                dst.fields(twinFieldRestDTOMapper.convertList(twinFieldEntityList, mapperProperties));
                return;
        }
    }

    public enum TwinMode implements MapperMode {
        NONE, ID_NAME_ONLY, DETAILED;

        public static final String _NONE = "NONE";
        public static final String _ID_NAME_ONLY = "ID_NAME_ONLY";
        public static final String _DETAILED = "DETAILED";
    }

    public enum FieldsMode implements MapperMode {
        NO_FIELDS, ALL_FIELDS, NOT_EMPTY_FIELDS;

        public static final String _NO_FIELDS = "NO_FIELDS";
        public static final String _ALL_FIELDS = "ALL_FIELDS";
        public static final String _NOT_EMPTY_FIELDS = "NOT_EMPTY_FIELDS";
    }
}
