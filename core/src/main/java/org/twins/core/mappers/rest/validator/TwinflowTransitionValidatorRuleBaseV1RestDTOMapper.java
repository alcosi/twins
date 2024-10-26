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

    @Override
    public void map(TwinflowTransitionValidatorRuleEntity src, TransitionValidatorRuleBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinflowTransitionValidatorRuleMode.HIDE)) {
            case DETAILED:
                dst
                        .setTwinflowTransitionId(src.getId())
                        .setId(src.getId())
                        .setOrder(src.getOrder())
                        .setActive(src.isActive())
                        .setTwinValidators(twinValidatorBaseV1RestDTOMapper.convertCollection(src.getTwinValidators(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinValidatorMode.TwinflowTransitionValidatorRule2TwinValidatorMode.HIDE))));
                break;
            case SHORT:
                dst
                        .setId(src.getId());

                break;
        }
        if (mapperContext.hasModeButNot(TwinValidatorSetMode.TwinflowTransitionValidatorRule2TwinValidatorSetMode.HIDE))
            dst
                    .setTwinValidatorSet(twinValidatorSetBaseV1RestDTOMapper.convert(
                            twinValidatorSetService.loadTwinValidatorSet(src), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinValidatorSetMode.TwinflowTransitionValidatorRule2TwinValidatorSetMode.HIDE))))
                    .setTwinValidatorSetId(src.getTwinValidatorSetId());

    }

    @Override
    public void beforeCollectionConversion(Collection<TwinflowTransitionValidatorRuleEntity> srcCollection, MapperContext mapperContext) throws ServiceException {
        if (mapperContext.hasModeButNot(TwinValidatorSetMode.TwinflowTransitionValidatorRule2TwinValidatorSetMode.HIDE))
            twinValidatorSetService.loadTwinValidatorSet(srcCollection);
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinflowTransitionValidatorRuleMode.HIDE);
    }

}
