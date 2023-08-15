package org.cambium.i18n.dao;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class I18nTypeConverter implements AttributeConverter<I18nType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(I18nType i18nType) {
        return i18nType.getId();
    }

    @Override
    public I18nType convertToEntityAttribute(Integer integer) {
        return I18nType.valueOd(integer);
    }
}
