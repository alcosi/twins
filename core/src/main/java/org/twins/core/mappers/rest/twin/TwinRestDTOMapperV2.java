package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dto.rest.twin.TwinDTOv2;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.service.twin.TwinService;

import java.util.List;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class TwinRestDTOMapperV2 extends RestSimpleDTOMapper<TwinEntity, TwinDTOv2> {
    final TwinBaseV3RestDTOMapper twinBaseV3RestDTOMapper;
    final TwinFieldRestDTOMapperV2 twinFieldRestDTOMapperV2;
    final TwinService twinService;

    @Override
    public void map(TwinEntity src, TwinDTOv2 dst, MapperContext mapperContext) throws Exception {
        twinBaseV3RestDTOMapper.map(src, dst, mapperContext);

        List<TwinFieldEntity> twinFieldEntityList;
        switch (mapperContext.getModeOrUse(TwinRestDTOMapper.FieldsMode.ALL_FIELDS)) {
            case NO_FIELDS:
                break;
            case ALL_FIELDS:
                twinFieldEntityList = twinService.findTwinFieldsIncludeMissing(src);
                dst.fields(twinFieldRestDTOMapperV2.convertList(twinFieldEntityList, mapperContext).stream().collect(Collectors
                        .toMap(
                                fieldValueText -> fieldValueText.getTwinClassField().getKey(),
                                FieldValueText::getValue)));
                break;
            case NOT_EMPTY_FIELDS:
                twinFieldEntityList = twinService.findTwinFields(src.getId());
                dst.fields(twinFieldRestDTOMapperV2.convertList(twinFieldEntityList, mapperContext).stream().collect(Collectors
                        .toMap(
                                fieldValueText -> fieldValueText.getTwinClassField().getKey(),
                                FieldValueText::getValue)));
                break;
        }
    }

    @Override
    public String getObjectCacheId(TwinEntity src) {
        return src.getId().toString();
    }
}
