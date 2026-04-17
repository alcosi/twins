package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.dto.rest.validator.TwinValidatorDTOv1;
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
@MapperModeBinding(modes = TwinValidatorMode.class)
public class TwinValidatorRestDTOMapper extends RestSimpleDTOMapper<TwinValidatorEntity, TwinValidatorDTOv1> {
    @MapperModePointerBinding(modes = TwinValidatorSetMode.TwinValidator2TwinValidatorSetMode.class)
    private final TwinValidatorSetRestDTOMapper twinValidatorSetRestDTOMapper;

    @MapperModePointerBinding(modes = FeaturerMode.TwinValidator2FeaturerMode.class)
    private final FeaturerRestDTOMapper featurerRestDTOMapper;

    private final TwinValidatorSetService twinValidatorSetService;

    @Override
    public void map(TwinValidatorEntity src, TwinValidatorDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinValidatorMode.DETAILED)) {
            case DETAILED -> dst
                    .setId(src.getId())
                    .setTwinValidatorSetId(src.getTwinValidatorSetId())
                    .setValidatorFeaturerId(src.getTwinValidatorFeaturerId())
                    .setValidatorParams(src.getTwinValidatorParams())
                    .setInvert(src.isInvert())
                    .setActive(src.getActive())
                    .setDescription(src.getDescription())
                    .setOrder(src.getOrder())
                    .setCreatedByUserId(src.getCreatedByUserId())
                    .setCreatedAt(src.getCreatedAt() != null ? src.getCreatedAt().toLocalDateTime() : null);
            case SHORT -> dst
                    .setId(src.getId())
                    .setTwinValidatorSetId(src.getTwinValidatorSetId());
        }

        if (mapperContext.hasModeButNot(TwinValidatorSetMode.TwinValidator2TwinValidatorSetMode.HIDE)) {
            dst.setTwinValidatorSetId(src.getTwinValidatorSetId());
            twinValidatorSetService.loadTwinValidatorSet(src);
            twinValidatorSetRestDTOMapper.postpone(src.getTwinValidatorSet(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinValidatorSetMode.TwinValidator2TwinValidatorSetMode.SHORT)));
        }

        if (mapperContext.hasModeButNot(FeaturerMode.TwinValidator2FeaturerMode.HIDE)) {
            dst.setValidatorFeaturerId(src.getTwinValidatorFeaturerId());
            twinValidatorSetService.loadTwinValidator(src);
            featurerRestDTOMapper.postpone(src.getTwinValidatorFeaturer(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(FeaturerMode.TwinValidator2FeaturerMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinValidatorEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(TwinValidatorSetMode.TwinValidator2TwinValidatorSetMode.HIDE)) {
            twinValidatorSetService.loadTwinValidatorSet(srcCollection);
        }
        if (mapperContext.hasModeButNot(FeaturerMode.TwinValidator2FeaturerMode.HIDE)) {
            twinValidatorSetService.loadTwinValidators(srcCollection);
        }
    }
}
