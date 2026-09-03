package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.validator.TwinClassFieldValidatorEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.twinclass.TwinClassFieldValidatorCountDTOv1;
import org.twins.core.enums.sort.TwinClassFieldValidatorGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassFieldMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassFieldValidatorMode;
import org.twins.core.service.twinclassfield.TwinClassFieldValidatorService;

import java.util.Collection;

@Component
@MapperModeBinding(modes = TwinClassFieldValidatorMode.class)
@RequiredArgsConstructor
public class TwinClassFieldValidatorCountRestDTOMapper
        extends RestSimpleDTOMapper<CountResult<TwinClassFieldValidatorEntity, TwinClassFieldValidatorGroupField>, TwinClassFieldValidatorCountDTOv1> {

    @MapperModePointerBinding(modes = TwinClassFieldMode.TwinClassFieldValidator2TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    @MapperModePointerBinding(modes = FeaturerMode.TwinClassFieldValidator2FeaturerMode.class)
    private final FeaturerRestDTOMapper featurerRestDTOMapper;

    private final TwinClassFieldValidatorService twinClassFieldValidatorService;

    @Override
    public void map(CountResult<TwinClassFieldValidatorEntity, TwinClassFieldValidatorGroupField> src, TwinClassFieldValidatorCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst.setCount(src.getCount());
        if (src.getGroupFields().contains(TwinClassFieldValidatorGroupField.twinClassFieldId))
            dst.setTwinClassFieldId(entity.getTwinClassFieldId());
        if (src.getGroupFields().contains(TwinClassFieldValidatorGroupField.fieldValidatorFeaturerId))
            dst.setFieldValidatorFeaturerId(entity.getFieldValidatorFeaturerId());
        if (needLoad(mapperContext, TwinClassFieldMode.TwinClassFieldValidator2TwinClassFieldMode.HIDE, src, TwinClassFieldValidatorGroupField.twinClassFieldId)) {
            twinClassFieldValidatorService.loadTwinClassField(entity);
            twinClassFieldRestDTOMapper.postpone(entity.getTwinClassField(), mapperContext.forkOnPoint(TwinClassFieldMode.TwinClassFieldValidator2TwinClassFieldMode.SHORT));
        }
        if (needLoad(mapperContext, FeaturerMode.TwinClassFieldValidator2FeaturerMode.HIDE, src, TwinClassFieldValidatorGroupField.fieldValidatorFeaturerId)) {
            featurerRestDTOMapper.postpone(entity.getFieldValidatorFeaturerId(), mapperContext.forkOnPoint(FeaturerMode.TwinClassFieldValidator2FeaturerMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<TwinClassFieldValidatorEntity, TwinClassFieldValidatorGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        var entities = srcCollection.stream().map(CountResult::getEntity).toList();
        if (entities.isEmpty()) {
            return;
        }
        var someCount = srcCollection.iterator().next();
        if (needLoad(mapperContext, TwinClassFieldMode.TwinClassFieldValidator2TwinClassFieldMode.HIDE, someCount, TwinClassFieldValidatorGroupField.twinClassFieldId)) {
            twinClassFieldValidatorService.loadTwinClassField(entities);
        }
    }
}
