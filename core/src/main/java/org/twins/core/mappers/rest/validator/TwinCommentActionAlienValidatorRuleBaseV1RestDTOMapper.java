package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.validator.TwinCommentActionAlienValidatorRuleEntity;
import org.twins.core.dto.rest.validator.TwinCommentActionAlienValidatorRuleBaseDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinCommentAlienValidatorRuleMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinValidatorMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinValidatorSetMode;
import org.twins.core.service.twin.TwinValidatorSetService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {TwinCommentAlienValidatorRuleMode.class})
public class TwinCommentActionAlienValidatorRuleBaseV1RestDTOMapper extends RestSimpleDTOMapper<TwinCommentActionAlienValidatorRuleEntity, TwinCommentActionAlienValidatorRuleBaseDTOv1> {

    @MapperModePointerBinding(modes = {TwinValidatorMode.TwinCommentActionAlienValidatorRule2TwinValidatorMode.class})
    private final TwinValidatorV1RestDTOMapper twinValidatorV1RestDTOMapper;

    @MapperModePointerBinding(modes = {TwinValidatorSetMode.TwinCommentActionAlienValidatorRule2TwinValidatorSetMode.class})
    private final TwinValidatorSetRestDTOMapper twinValidatorSetRestDTOMapper;

    private final TwinValidatorSetService twinValidatorSetService;

    @Override
    public void map(TwinCommentActionAlienValidatorRuleEntity src, TwinCommentActionAlienValidatorRuleBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinCommentAlienValidatorRuleMode.HIDE)) {
            case DETAILED:
                dst
                        .setTwinClassId(src.getTwinClassId())
                        .setCommentAction(src.getTwinCommentAction())
                        .setId(src.getId())
                        .setOrder(src.getOrder())
                        .setActive(src.isActive());
                break;
            case SHORT:
                dst
                        .setId(src.getId());
                break;

        }
        if (mapperContext.hasModeButNot(TwinValidatorSetMode.TwinCommentActionAlienValidatorRule2TwinValidatorSetMode.HIDE)) {
            twinValidatorSetService.loadTwinValidatorSet(src);
            dst
                    .setTwinValidatorSet(twinValidatorSetRestDTOMapper.convert(src.getTwinValidatorSet(), mapperContext.forkOnPoint(TwinValidatorMode.TwinCommentActionAlienValidatorRule2TwinValidatorMode.SHORT)))
                    .setTwinValidatorSetId(src.getTwinValidatorSetId());
        }
        if (mapperContext.hasModeButNot(TwinValidatorSetMode.TwinCommentActionAlienValidatorRule2TwinValidatorSetMode.HIDE)) {
            twinValidatorSetService.loadTwinValidatorSet(src);
            dst.setTwinValidators(twinValidatorV1RestDTOMapper.convertCollection(
                    src.getTwinValidatorKit().getList(), mapperContext.forkOnPoint(TwinValidatorMode.TwinCommentActionAlienValidatorRule2TwinValidatorMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinCommentActionAlienValidatorRuleEntity> srcCollection, MapperContext mapperContext) throws ServiceException {
        if (mapperContext.hasModeButNot(TwinValidatorSetMode.TwinCommentActionAlienValidatorRule2TwinValidatorSetMode.HIDE))
            twinValidatorSetService.loadTwinValidatorSet(srcCollection);
    }


    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinCommentAlienValidatorRuleMode.HIDE);
    }
}
