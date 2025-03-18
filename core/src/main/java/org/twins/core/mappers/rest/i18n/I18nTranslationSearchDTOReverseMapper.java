package org.twins.core.mappers.rest.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.I18nTranslationSearch;
import org.twins.core.dto.rest.i18n.I18nTranslationSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class I18nTranslationSearchDTOReverseMapper extends RestSimpleDTOMapper<I18nTranslationSearchRqDTOv1, I18nTranslationSearch> {
    @Override
    public void map(I18nTranslationSearchRqDTOv1 src, I18nTranslationSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setI18nIdList(src.getI18nIdList())
                .setI18nIdExcludeList(src.getI18nIdExcludeList())
                .setTranslationLikeList(src.getTranslationLikeList())
                .setTranslationNotLikeList(src.getTranslationNotLikeList())
                .setLocaleLikeList(src.getLocaleLikeList())
                .setLocaleNotLikeList(src.getLocaleNotLikeList())
                .setUsageCounter(src.getUsageCounter());
    }
}
