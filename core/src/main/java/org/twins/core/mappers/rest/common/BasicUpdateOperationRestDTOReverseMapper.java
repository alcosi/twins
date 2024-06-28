package org.twins.core.mappers.rest.common;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.domain.EntityRelinkOperation;
import org.twins.core.dto.rest.common.BasicUpdateOperationDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class BasicUpdateOperationRestDTOReverseMapper extends RestSimpleDTOMapper<BasicUpdateOperationDTOv1, EntityRelinkOperation> {
    final I18nService i18nService;

    @Override
    public void map(BasicUpdateOperationDTOv1 src, EntityRelinkOperation dst, MapperContext mapperContext) throws Exception {
        dst
                .setNewId(src.getNewId())
                .setStrategy(src.getOnUnreplacedStrategy())
                .setReplaceMap(src.getReplaceMap())
        ;
    }
}
