package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.dto.rest.validator.TwinValidatorBaseDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinValidatorMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinValidatorSetMode;
import org.twins.core.service.twin.TwinValidatorSetService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {TwinValidatorMode.class})
public class TwinValidatorBaseV1RestDTOMapper extends RestSimpleDTOMapper<TwinValidatorEntity, TwinValidatorBaseDTOv1> {

    @MapperModePointerBinding(modes = {TwinValidatorSetMode.TwinValidator2TwinValidatorSetMode.class})
    private final TwinValidatorSetBaseV1RestDTOMapper twinValidatorSetBaseV1RestDTOMapper;

    @MapperModePointerBinding(modes = FeaturerMode.TwinValidator2FeaturerMode.class)
    private final FeaturerRestDTOMapper featurerRestDTOMapper;

    private final TwinValidatorSetService twinValidatorSetService;

    @Override
    public void map(TwinValidatorEntity src, TwinValidatorBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinValidatorMode.SHORT)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setOrder(src.getOrder())
                        .setDescription(src.getDescription())
                        .setValidatorFeaturerId(src.getTwinValidatorFeaturerId())
                        .setValidatorParams(src.getTwinValidatorParams())
                        .setInvert(src.isInvert())
                        .setActive(src.isActive())
                        .setTwinValidatorSetId(src.getTwinValidatorSetId());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setTwinValidatorSetId(src.getTwinValidatorSetId());
                break;
        }
        if (mapperContext.hasModeButNot(TwinValidatorSetMode.TwinValidator2TwinValidatorSetMode.HIDE))
            dst
                    .setTwinValidatorSet(twinValidatorSetBaseV1RestDTOMapper.convert(
                            twinValidatorSetService.loadTwinValidatorSet(src), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinValidatorSetMode.TwinValidator2TwinValidatorSetMode.SHORT))))
                    .setTwinValidatorSetId(src.getTwinValidatorSetId());
        if (mapperContext.hasModeButNot(FeaturerMode.TwinValidator2FeaturerMode.HIDE))
            dst
                    .setValidatorFeaturer(featurerRestDTOMapper.convertOrPostpone(src.getTwinValidatorFeaturer(), mapperContext.forkOnPoint(FeaturerMode.TwinValidator2FeaturerMode.SHORT)))
                    .setTwinValidatorSetId(src.getTwinValidatorSetId());
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinValidatorEntity> srcCollection, MapperContext mapperContext) throws ServiceException {
        if (mapperContext.hasModeButNot(TwinValidatorSetMode.TwinValidator2TwinValidatorSetMode.HIDE))
            twinValidatorSetService.loadTwinValidatorSet(srcCollection);
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinValidatorMode.HIDE);
    }

}
