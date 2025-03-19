package org.twins.core.mappers.rest.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.domain.i18n.I18nTranslation;
import org.twins.core.dto.rest.i18n.I18nTranslationUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class I18nTranslationUpdateDTOReverseMapper extends RestSimpleDTOMapper<I18nTranslationUpdateDTOv1, ArrayList<I18nTranslation>> {
    private final I18nTranslationSaveDTOReverseMapper i18nTranslationSaveDTOReverseMapper;

    @Override
    public void map(I18nTranslationUpdateDTOv1 src, ArrayList<I18nTranslation> dst, MapperContext mapperContext) throws Exception {
        i18nTranslationSaveDTOReverseMapper.map(src, dst, mapperContext);
        for (I18nTranslation e : dst) {
            e.setI18nId(src.getI18nId());
        }
    }
}
