package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.validator.ValidatorRule;
import org.twins.core.dto.rest.validator.ValidatorRuleBaseDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.service.twin.TwinValidatorSetService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {ValidatorRuleMode.class})
public class ValidatorRuleBaseV1RestDTOMapper extends RestSimpleDTOMapper<ValidatorRule, ValidatorRuleBaseDTOv1> {

    @MapperModePointerBinding(modes = {TwinValidatorMode.ValidatorRule2TwinValidatorMode.class})
    private final TwinValidatorBaseV1RestDTOMapper twinValidatorBaseV1RestDTOMapper;

    @MapperModePointerBinding(modes = {TwinValidatorSetMode.ValidatorRule2TwinValidatorSetMode.class})
    private final TwinValidatorSetBaseV1RestDTOMapper twinValidatorSetBaseV1RestDTOMapper;

    private final TwinValidatorSetService twinValidatorSetService;

    @Override
    public void map(ValidatorRule src, ValidatorRuleBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(ValidatorRuleMode.HIDE)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setOrder(src.getOrder())
                        .setActive(src.isActive())
                        .setTwinValidators(twinValidatorBaseV1RestDTOMapper.convertCollection(src.getTwinValidators(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinValidatorMode.ValidatorRule2TwinValidatorMode.HIDE))));
                break;
            case SHORT:
                dst
                        .setId(src.getId());
                break;
        }
        if (mapperContext.hasModeButNot(TwinValidatorSetMode.ValidatorRule2TwinValidatorSetMode.HIDE))
            dst
                    .setTwinValidatorSet(twinValidatorSetBaseV1RestDTOMapper.convert(
                            twinValidatorSetService.loadTwinValidatorSet(src), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinValidatorSetMode.ValidatorRule2TwinValidatorSetMode.HIDE))))
                    .setTwinValidatorSetId(src.getTwinValidatorSetId());
    }

    @Override
    public void beforeCollectionConversion(Collection<ValidatorRule> srcCollection, MapperContext mapperContext) throws ServiceException {
        if (mapperContext.hasModeButNot(TwinValidatorSetMode.ValidatorRule2TwinValidatorSetMode.HIDE))
            twinValidatorSetService.loadTwinValidatorSetForValidators(srcCollection);
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(ValidatorRuleMode.HIDE);
    }

}
