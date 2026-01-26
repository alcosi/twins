package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.validator.TwinflowTransitionValidatorRuleEntity;
import org.twins.core.dto.rest.validator.TransitionValidatorRuleBaseDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinValidatorMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinValidatorSetMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinflowTransitionValidatorRuleMode;
import org.twins.core.service.twin.TwinValidatorSetService;
import org.twins.core.service.validator.TwinValidatorService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {TwinflowTransitionValidatorRuleMode.class})
public class TwinflowTransitionValidatorRuleBaseV1RestDTOMapper extends RestSimpleDTOMapper<TwinflowTransitionValidatorRuleEntity, TransitionValidatorRuleBaseDTOv1> {

    @MapperModePointerBinding(modes = {TwinValidatorMode.TwinflowTransitionValidatorRule2TwinValidatorMode.class})
    private final TwinValidatorBaseV1RestDTOMapper twinValidatorBaseV1RestDTOMapper;

    @MapperModePointerBinding(modes = {TwinValidatorSetMode.TwinflowTransitionValidatorRule2TwinValidatorSetMode.class})
    private final TwinValidatorSetBaseV1RestDTOMapper twinValidatorSetBaseV1RestDTOMapper;

    private final TwinValidatorSetService twinValidatorSetService;
    private final TwinValidatorService twinValidatorService;

    @Override
    public void map(TwinflowTransitionValidatorRuleEntity src, TransitionValidatorRuleBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinflowTransitionValidatorRuleMode.HIDE)) {
            case DETAILED:
                dst
                        .setTwinflowTransitionId(src.getId())
                        .setId(src.getId())
                        .setOrder(src.getOrder())
                        .setActive(src.isActive());
                break;
            case SHORT:
                dst
                        .setId(src.getId());
                break;
        }
        if (mapperContext.hasModeButNot(TwinValidatorSetMode.TwinflowTransitionValidatorRule2TwinValidatorSetMode.HIDE))
            dst
                    .setTwinValidatorSet(twinValidatorSetBaseV1RestDTOMapper.convert(
                           src.getTwinValidatorSet(), mapperContext.forkOnPoint(TwinValidatorSetMode.TwinflowTransitionValidatorRule2TwinValidatorSetMode.SHORT)))
                    .setTwinValidatorSetId(src.getTwinValidatorSetId());
        if (mapperContext.hasModeButNot(TwinValidatorMode.TwinflowTransitionValidatorRule2TwinValidatorMode.HIDE)) {
            twinValidatorService.loadValidators(src);
            dst.setTwinValidators(twinValidatorBaseV1RestDTOMapper.convertCollection(
                    src.getTwinValidatorKit().getList(), mapperContext.forkOnPoint(TwinValidatorMode.TwinflowTransitionValidatorRule2TwinValidatorMode.SHORT)));
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinflowTransitionValidatorRuleMode.HIDE);
    }

}
