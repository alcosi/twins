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
    private final TwinValidatorSetBaseV1RestDTOMapper twinValidatorSetBaseV1RestDTOMapper;

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
                        .setActive(src.isActive())
                        .setTwinValidators(twinValidatorBaseV1RestDTOMapper.convertCollection(src.getTwinValidators(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinValidatorMode.TwinActionValidatorRule2TwinValidatorMode.HIDE))));
                break;
            case SHORT:
                dst
                        .setId(src.getId());

                break;
        }
        if (mapperContext.hasModeButNot(TwinValidatorSetMode.TwinActionValidatorRule2TwinValidatorSetMode.HIDE))
            dst
                    .setTwinValidatorSet(twinValidatorSetBaseV1RestDTOMapper.convert(
                            twinValidatorSetService.loadTwinValidatorSet(src), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinValidatorSetMode.TwinActionValidatorRule2TwinValidatorSetMode.HIDE))))
                    .setTwinValidatorSetId(src.getTwinValidatorSetId());
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinActionValidatorRuleEntity> srcCollection, MapperContext mapperContext) throws ServiceException {
        if (mapperContext.hasModeButNot(TwinValidatorSetMode.TwinActionValidatorRule2TwinValidatorSetMode.HIDE))
            twinValidatorSetService.loadTwinValidatorSet(srcCollection);
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinActionValidatorRuleMode.HIDE);
    }

}
