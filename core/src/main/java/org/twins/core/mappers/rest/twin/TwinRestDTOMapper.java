package org.twins.core.mappers.rest.twin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dto.rest.twin.TwinDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.service.twin.TwinService;

import java.util.List;


@Component
@RequiredArgsConstructor
public class TwinRestDTOMapper extends RestSimpleDTOMapper<TwinEntity, TwinDTOv1> {
    final TwinBaseV3RestDTOMapper twinBaseV3RestDTOMapper;
    final TwinFieldRestDTOMapper twinFieldRestDTOMapper;
    final TwinService twinService;


    @Override
    public void map(TwinEntity src, TwinDTOv1 dst, MapperContext mapperContext) throws Exception {
        twinBaseV3RestDTOMapper.map(src, dst, mapperContext);

        List<TwinFieldEntity> twinFieldEntityList;
        switch (mapperContext.getModeOrUse(FieldsMode.ALL_FIELDS)) {
            case NO_FIELDS:
                break;
            case ALL_FIELDS:
                twinFieldEntityList = twinService.findTwinFieldsIncludeMissing(src);
                dst.fields(twinFieldRestDTOMapper.convertList(twinFieldEntityList, mapperContext));
                break;
            case NOT_EMPTY_FIELDS:
                twinFieldEntityList = twinService.findTwinFields(src.getId());
                dst.fields(twinFieldRestDTOMapper.convertList(twinFieldEntityList, mapperContext));
                break;
            case ALL_FIELDS_WITH_ATTACHMENTS:
                twinFieldEntityList = twinService.findTwinFieldsIncludeMissing(src);
                dst.fields(twinFieldRestDTOMapper.convertList(twinFieldEntityList, mapperContext));
                break;
            case NOT_EMPTY_FIELDS_WITH_ATTACHMENTS:
                twinFieldEntityList = twinService.findTwinFields(src.getId());
                dst.fields(twinFieldRestDTOMapper.convertList(twinFieldEntityList, mapperContext));
                break;
        }
    }

    @Override
    public String getObjectCacheId(TwinEntity src) {
        return src.getId().toString();
    }

    @AllArgsConstructor
    public enum FieldsMode implements MapperMode {
        NO_FIELDS(0),
        NOT_EMPTY_FIELDS(1),
        ALL_FIELDS(2),
        NOT_EMPTY_FIELDS_WITH_ATTACHMENTS(3),
        ALL_FIELDS_WITH_ATTACHMENTS(4);

        public static final String _NO_FIELDS = "NO_FIELDS";
        public static final String _ALL_FIELDS = "ALL_FIELDS";
        public static final String _NOT_EMPTY_FIELDS = "NOT_EMPTY_FIELDS";
        public static final String _ALL_FIELDS_WITH_ATTACHMENTS = "ALL_FIELDS_WITH_ATTACHMENTS";
        public static final String _NOT_EMPTY_FIELDS_WITH_ATTACHMENTS = "NOT_EMPTY_FIELDS_WITH_ATTACHMENTS";

        @Getter
        final int priority;

    }
}
