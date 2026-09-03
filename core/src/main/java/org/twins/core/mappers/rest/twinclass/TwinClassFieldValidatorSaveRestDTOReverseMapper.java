package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.validator.TwinClassFieldValidatorEntity;
import org.twins.core.domain.twinclass.TwinClassFieldValidatorSave;
import org.twins.core.dto.rest.twinclass.TwinClassFieldValidatorSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinClassFieldValidatorSaveRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFieldValidatorSaveDTOv1, TwinClassFieldValidatorSave> {
    private final I18nSaveRestDTOReverseMapper i18nSaveRestDTOReverseMapper;

    @Override
    public void map(TwinClassFieldValidatorSaveDTOv1 src, TwinClassFieldValidatorSave dst, MapperContext mapperContext) throws Exception {
        dst
                .setBeValidationErrorI18n(i18nSaveRestDTOReverseMapper.convert(src.getBeValidationErrorI18n()))
                .setTwinClassFieldValidator(
                        new TwinClassFieldValidatorEntity()
                                .setTwinClassFieldId(src.getTwinClassFieldId())
                                .setFieldValidatorFeaturerId(src.getFieldValidatorFeaturerId())
                                .setFieldValidatorParams(src.getFieldValidatorParams())
                );
    }
}
