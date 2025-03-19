package org.twins.core.mappers.rest.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.i18n.I18nSave;
import org.twins.core.dto.rest.i18n.I18nTranslationSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.ArrayList;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class I18nTranslationSaveDTOReverseMapper extends RestSimpleDTOMapper<I18nTranslationSaveDTOv1, ArrayList<I18nSave>> {
    @Override
    public void map(I18nTranslationSaveDTOv1 src, ArrayList<I18nSave> dst, MapperContext mapperContext) throws Exception {
        for (var entry : src.getTranslations().entrySet()) {
            if (entry.getKey() == null)
                continue;

            I18nSave entity = new I18nSave()
                    .setTranslations(Map.of(entry.getKey(), entry.getValue()));

            dst.add(entity);
        }
    }
}