package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.domain.TwinField;
import org.twins.core.dto.rest.twin.TwinFieldAttributeDTOv1;
import org.twins.core.dto.rest.twin.TwinFieldDTOv4;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinFieldAttributeMode;
import org.twins.core.service.twin.TwinFieldAttributeService;
import org.twins.core.service.twin.TwinService;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class TwinFieldRestDTOMapperV4 extends RestSimpleDTOMapper<TwinField, TwinFieldDTOv4> {

    private final TwinFieldValueRestDTOMapperV2 twinFieldValueRestDTOMapperV2;

    @Lazy
    private final TwinFieldAttributeService twinFieldAttributeService;
    @Lazy
    private final TwinService twinService;
    private final TwinFieldAttributeRestDTOMapper twinFieldAttributeRestDTOMapper;

    @Override
    public void map(TwinField src, TwinFieldDTOv4 dst, MapperContext mapperContext) throws Exception {
        FieldValue fieldValue = twinService.getTwinFieldValue(src);
        var fieldValueText = twinFieldValueRestDTOMapperV2.convert(fieldValue, mapperContext);
        String fieldKey = src.getTwinClassField().getKey();
        String fieldValueStr = fieldValueText.getValue();

        dst
                .setId(src.getTwinClassFieldId())
                .setKey(fieldKey)
                .setValue(fieldValueStr);

        if (mapperContext.hasMode(TwinFieldAttributeMode.SHOW)) {
            twinFieldAttributeService.loadFieldAttributes(src);
            Map<UUID, TwinFieldAttributeDTOv1> fieldAttributesMap = twinFieldAttributeRestDTOMapper.convertCollection(src.getAttributes().getCollection(), mapperContext)
                    .stream()
                    .collect(Collectors.toMap(TwinFieldAttributeDTOv1::getId, Function.identity()));

            dst.setFieldAttributes(fieldAttributesMap);
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinField> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasMode(TwinFieldAttributeMode.SHOW)) {
            twinFieldAttributeService.loadFieldAttributes(srcCollection);
        }
    }
}
