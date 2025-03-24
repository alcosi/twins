package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.twins.core.service.i18n.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dto.rest.datalist.DataListAttributeDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.UUID;


@Component
@RequiredArgsConstructor
public class DataListAttributeRestDTOMapper extends RestSimpleDTOMapper<ImmutablePair<String, UUID>, DataListAttributeDTOv1> {

    private final I18nService i18nService;

    @Override
    public void map(ImmutablePair<String, UUID> src, DataListAttributeDTOv1 dst, MapperContext mapperContext) throws Exception {
            dst
                .setKey(src.getLeft())
                .setName(i18nService.translateToLocale(src.getRight()));
    }
}
