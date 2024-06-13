package org.twins.core.mappers.rest.common;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.UuidUtils;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.domain.ReplaceOperation;
import org.twins.core.dto.rest.ReplaceOperationDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

import java.util.UUID;


@Component
@RequiredArgsConstructor
public class ReplaceOperationRestDTOReverseMapper extends RestSimpleDTOMapper<ReplaceOperationDTOv1, ReplaceOperation> {
    final I18nService i18nService;

    @Override
    public void map(ReplaceOperationDTOv1 src, ReplaceOperation dst, MapperContext mapperContext) throws Exception {
        if (CollectionUtils.isNotEmpty(src.getDeleteSet()))
            for (UUID id : src.getDeleteSet())
                src.getReplaceMap().put(id, UuidUtils.NULLIFY_MARKER);
        dst
                .setStrategy(src.getStrategy())
                .setReplaceMap(src.getReplaceMap())
        ;
    }
}
