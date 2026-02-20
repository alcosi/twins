package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowTransitionAliasEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dto.rest.twinflow.TransitionSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class TransitionSaveRestDTOReverseMapper extends RestSimpleDTOMapper<TransitionSaveDTOv1, TwinflowTransitionEntity> {

    @Override
    public void map(TransitionSaveDTOv1 src, TwinflowTransitionEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setSrcTwinStatusId(src.getSrcStatusId())
                .setDstTwinStatusId(src.getDstStatusId())
                .setPermissionId(src.getPermissionId())
                .setTwinflowId(src.getTwinflowId())
                .setInbuiltTwinFactoryId(src.getInbuiltTwinFactoryId())
                .setDraftingTwinFactoryId(src.getDraftingTwinFactoryId())
                .setTwinflowTransitionTypeId(src.getTwinflowTransitionTypeId())
                .setTwinflowTransitionAlias(new TwinflowTransitionAliasEntity().setAlias(src.getAlias()));

    }
}
