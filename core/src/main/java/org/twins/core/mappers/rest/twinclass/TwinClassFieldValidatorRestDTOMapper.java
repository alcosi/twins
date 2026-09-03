package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.validator.TwinClassFieldValidatorEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldValidatorDTOv1;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassFieldMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassFieldValidatorMode;
import org.twins.core.service.twinclassfield.TwinClassFieldValidatorService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {TwinClassFieldValidatorMode.class})
public class TwinClassFieldValidatorRestDTOMapper extends RestSimpleDTOMapper<TwinClassFieldValidatorEntity, TwinClassFieldValidatorDTOv1> {

    @MapperModePointerBinding(modes = {TwinClassFieldMode.TwinClassFieldValidator2TwinClassFieldMode.class})
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    @MapperModePointerBinding(modes = FeaturerMode.TwinClassFieldValidator2FeaturerMode.class)
    private final FeaturerRestDTOMapper featurerRestDTOMapper;

    private final TwinClassFieldValidatorService twinClassFieldValidatorService;

    @Override
    public void map(TwinClassFieldValidatorEntity src, TwinClassFieldValidatorDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinClassFieldValidatorMode.SHORT)) {
            case DETAILED -> dst
                    .setId(src.getId())
                    .setTwinClassFieldId(src.getTwinClassFieldId())
                    .setFieldValidatorFeaturerId(src.getFieldValidatorFeaturerId())
                    .setFieldValidatorParams(src.getFieldValidatorParams())
                    .setBeValidationErrorI18nId(src.getBeValidationErrorI18nId())
                    .setBeValidationError(I18nCacheHolder.addId(src.getBeValidationErrorI18nId()));
            case SHORT -> dst
                    .setId(src.getId())
                    .setTwinClassFieldId(src.getTwinClassFieldId());
        }
        if (mapperContext.hasModeButNot(TwinClassFieldMode.TwinClassFieldValidator2TwinClassFieldMode.HIDE)) {
            dst.setTwinClassFieldId(src.getTwinClassFieldId());
            twinClassFieldValidatorService.loadTwinClassField(src);
            twinClassFieldRestDTOMapper.postpone(src.getTwinClassField(), mapperContext.forkOnPoint(TwinClassFieldMode.TwinClassFieldValidator2TwinClassFieldMode.SHORT));
        }
        if (mapperContext.hasModeButNot(FeaturerMode.TwinClassFieldValidator2FeaturerMode.HIDE)) {
            dst.setFieldValidatorFeaturerId(src.getFieldValidatorFeaturerId());
            featurerRestDTOMapper.postpone(src.getFieldValidatorFeaturerId(), mapperContext.forkOnPoint(FeaturerMode.TwinClassFieldValidator2FeaturerMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinClassFieldValidatorEntity> srcCollection, MapperContext mapperContext) throws ServiceException {
        if (mapperContext.hasModeButNot(TwinClassFieldMode.TwinClassFieldValidator2TwinClassFieldMode.HIDE))
            twinClassFieldValidatorService.loadTwinClassField(srcCollection);
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinClassFieldValidatorMode.HIDE);
    }
}
