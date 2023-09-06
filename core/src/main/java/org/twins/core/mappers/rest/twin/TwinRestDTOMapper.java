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
    final TwinFieldValueRestDTOMapper twinFieldValueRestDTOMapper;
    final TwinService twinService;

    @Override
    public void map(TwinEntity src, TwinDTOv1 dst, MapperProperties mapperProperties) throws Exception {
        dst
                .id(src.id())
                .name(src.name())
                .description(src.description())
                .assignerUser(userDTOMapper.convert(src.assignerUser(), mapperProperties))
                .authorUser(userDTOMapper.convert(src.createdByUser(), mapperProperties))
                .status(twinStatusRestDTOMapper.convert(src.twinStatus(), mapperProperties))
                .twinClass(twinClassRestDTOMapper.convert(src.twinClass(), mapperProperties))
                .createdAt(src.createdAt().toInstant())
        ;
        List<TwinFieldEntity> twinFieldEntityList;
        switch (mapperProperties.getModeOrUse(Mode.FIELDS_VALUES)) {
            case NO_FIELDS_VALUES:
                return;
            case FIELDS_VALUES:
                twinFieldEntityList = twinService.findTwinFieldsAll(src);
                dst.fields(twinFieldValueRestDTOMapper.convertList(twinFieldEntityList, mapperProperties));
                return;
            case FIELDS_VALUES_HIDE_EMPTY:
                twinFieldEntityList = twinService.findTwinFields(src.id());
                dst.fields(twinFieldValueRestDTOMapper.convertList(twinFieldEntityList, mapperProperties));
                return;
        }
    }

    public enum Mode implements MapperMode {
        NO_FIELDS_VALUES, FIELDS_VALUES, FIELDS_VALUES_HIDE_EMPTY;

        public static final String _NO_FIELDS_VALUES = "NO_FIELDS_VALUES";
        public static final String _FIELDS_VALUES = "FIELDS_VALUES";
        public static final String _FIELDS_VALUES_HIDE_EMPTY = "FIELDS_VALUES_HIDE_EMPTY";
    }
}
