package org.twins.core.mappers.rest.common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.EntityRelinkOperation;
import org.twins.core.dto.rest.common.BasicUpdateOperationDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class BasicUpdateOperationRestDTOReverseMapper extends RestSimpleDTOMapper<BasicUpdateOperationDTOv1, EntityRelinkOperation> {

    @Override
    public void map(BasicUpdateOperationDTOv1 src, EntityRelinkOperation dst, MapperContext mapperContext) throws Exception {
        dst
                .setNewId(src.getNewId())
                .setStrategy(src.getOnUnreplacedStrategy())
                .setReplaceMap(src.getReplaceMap())
        ;
    }
}
