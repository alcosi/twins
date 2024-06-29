package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dto.rest.twin.TwinDTOv1;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.service.twin.TwinService;

import java.util.List;


@Component
@RequiredArgsConstructor
public class TwinRestDTOMapper extends RestSimpleDTOMapper<TwinEntity, TwinDTOv1> {
    final TwinBaseV3RestDTOMapper twinBaseV3RestDTOMapper;
    final TwinFieldRestDTOMapperV3 twinFieldRestDTOMapperV3;
    final TwinService twinService;


    @Override
    public void map(TwinEntity src, TwinDTOv1 dst, MapperContext mapperContext) throws Exception {
        twinBaseV3RestDTOMapper.map(src, dst, mapperContext);

        switch (mapperContext.getModeOrUse(MapperMode.TwinFieldCollectionMode.ALL_FIELDS)) {
            case NO_FIELDS:
                break;
            case ALL_FIELDS, ALL_FIELDS_WITH_ATTACHMENTS:
                twinService.loadFieldsValues(src);
                dst.fields(twinFieldRestDTOMapperV3.convertCollection(src.getFieldValuesKit().getCollection(), mapperContext));
                break;
            case NOT_EMPTY_FIELDS, NOT_EMPTY_FIELDS_WITH_ATTACHMENTS:
                twinService.loadFieldsValues(src);
                List<FieldValue> notEmptyFields = src.getFieldValuesKit().getCollection().stream().filter(FieldValue::isFilled).toList();
                dst.fields(twinFieldRestDTOMapperV3.convertCollection(notEmptyFields, mapperContext));
                break;
        }
    }

    @Override
    public String getObjectCacheId(TwinEntity src) {
        return src.getId().toString();
    }

}
