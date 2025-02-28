//package org.twins.core.mappers.rest.i18n;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//import org.twins.core.dao.i18n.I18nTranslationEntity;
//import org.twins.core.dto.rest.i18n.I18nTranslationUpdateDTOv1;
//import org.twins.core.mappers.rest.RestSimpleDTOMapper;
//import org.twins.core.mappers.rest.mappercontext.MapperContext;
//
//@Component
//@RequiredArgsConstructor
//public class I18nTranslationUpdateDTOReverseMapper extends RestSimpleDTOMapper<I18nTranslationUpdateDTOv1, I18nTranslationEntity> {
//    private final I18nTranslationSaveDTOReverseMapper i18nTranslationSaveDTOReverseMapper;
//
//    @Override
//    public void map(I18nTranslationUpdateDTOv1 src, I18nTranslationEntity dst, MapperContext mapperContext) throws Exception {
//        i18nTranslationSaveDTOReverseMapper.map(src, dst, mapperContext);
//    }
//}
