package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.validator.TwinActionValidatorRuleEntity;
import org.twins.core.dto.rest.validator.TwinActionValidatorRuleBaseDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinActionValidatorRuleMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinValidatorMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinValidatorSetMode;
import org.twins.core.service.twin.TwinValidatorSetService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {TwinActionValidatorRuleMode.class})
public class TwinActionValidatorRuleBaseV1RestDTOMapper extends RestSimpleDTOMapper<TwinActionValidatorRuleEntity, TwinActionValidatorRuleBaseDTOv1> {

    @MapperModePointerBinding(modes = {TwinValidatorMode.TwinActionValidatorRule2TwinValidatorMode.class})
    private final TwinValidatorBaseV1RestDTOMapper twinValidatorBaseV1RestDTOMapper;

    @MapperModePointerBinding(modes = {TwinValidatorSetMode.TwinActionValidatorRule2TwinValidatorSetMode.class})
    private final TwinValidatorSetRestDTOMapper twinValidatorSetRestDTOMapper;

    private final TwinValidatorSetService twinValidatorSetService;

    @Override
    public void map(TwinActionValidatorRuleEntity src, TwinActionValidatorRuleBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinActionValidatorRuleMode.HIDE)) {
            case DETAILED:
                dst
                        .setTwinClassId(src.getTwinClassId())
                        .setTwinAction(src.getTwinAction())
                        .setId(src.getId())
                        .setOrder(src.getOrder())
                        .setActive(src.isActive());
                break;
            case SHORT:
                dst
                        .setId(src.getId());
                break;
        }
        if (mapperContext.hasModeButNot(TwinValidatorSetMode.TwinActionValidatorRule2TwinValidatorSetMode.HIDE))
            dst
                    .setTwinValidatorSet(twinValidatorSetRestDTOMapper.convert(
                            src.getTwinValidatorSet(), mapperContext.forkOnPoint(TwinValidatorSetMode.TwinActionValidatorRule2TwinValidatorSetMode.SHORT)))
                    .setTwinValidatorSetId(src.getTwinValidatorSetId());
        if (mapperContext.hasModeButNot(TwinValidatorMode.TwinActionValidatorRule2TwinValidatorMode.HIDE)) {
            dst.setTwinValidators(twinValidatorBaseV1RestDTOMapper.convertCollection(
                    src.getTwinValidatorKit().getList(), mapperContext.forkOnPoint(TwinValidatorMode.TwinActionValidatorRule2TwinValidatorMode.SHORT)));
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinActionValidatorRuleMode.HIDE);
    }

}
