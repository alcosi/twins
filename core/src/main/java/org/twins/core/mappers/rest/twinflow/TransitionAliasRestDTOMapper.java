package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.twinflow.TwinflowTransitionAliasEntity;
import org.twins.core.dto.rest.transition.TransitionAliasDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TransitionAliasMode;
import org.twins.core.service.twinflow.TwinflowTransitionService;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TransitionAliasMode.class)
public class TransitionAliasRestDTOMapper extends RestSimpleDTOMapper<TwinflowTransitionAliasEntity, TransitionAliasDTOv1> {

    private final TwinflowTransitionService twinflowTransitionService;

    @Override
    public void map(TwinflowTransitionAliasEntity src, TransitionAliasDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TransitionAliasMode.SHORT)) {
            case DETAILED -> {
                twinflowTransitionService.countUsagesInTwinflowTransition(src);
                dst
                        .setId(src.getId())
                        .setAlias(src.getAlias())
                        .setUsagesCount(src.getInTwinflowTransitionUsagesCount());
            }
            case SHORT -> dst
                    .setId(src.getId())
                    .setAlias(src.getAlias());
        }
    }
}
