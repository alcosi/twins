package org.twins.core.mappers.rest.i18n;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.dto.rest.i18n.I18nTranslationSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class I18nTranslationSaveDTOReverseMapper extends RestSimpleDTOMapper<I18nTranslationSaveDTOv1, ArrayList<I18nTranslationEntity>> {
    @Override
    public void map(I18nTranslationSaveDTOv1 src, ArrayList<I18nTranslationEntity> dst, MapperContext mapperContext) throws Exception {
        for (var entry : src.getTranslations().entrySet()) {
            if (entry.getKey() == null)
                continue;

            I18nTranslationEntity entity = new I18nTranslationEntity()
                    .setLocale(entry.getKey())
                    .setTranslation(entry.getValue());

            dst.add(entity);
        }
    }
}