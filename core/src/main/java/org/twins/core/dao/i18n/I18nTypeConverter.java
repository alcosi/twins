package org.twins.core.dao.i18n;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class I18nTypeConverter implements AttributeConverter<I18nType, String> {
    @Override
    public String convertToDatabaseColumn(I18nType i18nType) {
        return i18nType.getId();
    }

    @Override
    public I18nType convertToEntityAttribute(String id) {
        return I18nType.valueOd(id);
    }
}
