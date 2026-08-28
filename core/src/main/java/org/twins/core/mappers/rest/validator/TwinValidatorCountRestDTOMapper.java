package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.validator.TwinValidatorCountDTOv1;
import org.twins.core.enums.sort.TwinValidatorGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinValidatorMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinValidatorSetMode;
import org.twins.core.service.twinvalidator.TwinValidatorService;

import java.util.Collection;

@Component
@MapperModeBinding(modes = TwinValidatorMode.class)
@RequiredArgsConstructor
public class TwinValidatorCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<TwinValidatorEntity, TwinValidatorGroupField>, TwinValidatorCountDTOv1> {

    @MapperModePointerBinding(modes = TwinValidatorSetMode.TwinValidator2TwinValidatorSetMode.class)
    private final TwinValidatorSetRestDTOMapper twinValidatorSetRestDTOMapper;

    @MapperModePointerBinding(modes = FeaturerMode.TwinValidator2FeaturerMode.class)
    private final FeaturerRestDTOMapper featurerRestDTOMapper;

    private final TwinValidatorService twinValidatorService;

    @Override
    public void map(CountResult<TwinValidatorEntity, TwinValidatorGroupField> src, TwinValidatorCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst.setCount(src.getCount());
        if (src.getGroupFields().contains(TwinValidatorGroupField.invert))
            dst.setInvert(entity.getInvert());
        if (src.getGroupFields().contains(TwinValidatorGroupField.active))
            dst.setActive(entity.getActive());
        if (src.getGroupFields().contains(TwinValidatorGroupField.twinValidatorSetId))
            dst.setTwinValidatorSetId(entity.getTwinValidatorSetId());
        if (src.getGroupFields().contains(TwinValidatorGroupField.validatorFeaturerId))
            dst.setValidatorFeaturerId(entity.getTwinValidatorFeaturerId());
        if (needLoad(mapperContext, TwinValidatorSetMode.TwinValidator2TwinValidatorSetMode.HIDE, src, TwinValidatorGroupField.twinValidatorSetId)) {
            twinValidatorService.loadTwinValidatorSet(entity);
            twinValidatorSetRestDTOMapper.postpone(entity.getTwinValidatorSet(), mapperContext.forkOnPoint(TwinValidatorSetMode.TwinValidator2TwinValidatorSetMode.SHORT));
        }
        if (needLoad(mapperContext, FeaturerMode.TwinValidator2FeaturerMode.HIDE, src, TwinValidatorGroupField.validatorFeaturerId)) {
            featurerRestDTOMapper.postpone(entity.getTwinValidatorFeaturerId(), mapperContext.forkOnPoint(FeaturerMode.TwinValidator2FeaturerMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<TwinValidatorEntity, TwinValidatorGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        var entities = srcCollection.stream().map(CountResult::getEntity).toList();
        if (entities.isEmpty()) {
            return;
        }
        var someCount = srcCollection.iterator().next();
        if (needLoad(mapperContext, TwinValidatorSetMode.TwinValidator2TwinValidatorSetMode.HIDE, someCount, TwinValidatorGroupField.twinValidatorSetId)) {
            twinValidatorService.loadTwinValidatorSet(entities);
        }
        // Featurer is postponed by id in map() — no batch load needed here.
    }
}
