package org.twins.core.dao;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class CUDConverter implements AttributeConverter<CUD, String> {
    @Override
    public String convertToDatabaseColumn(CUD action) {
        return action.getId();
    }

    @Override
    public CUD convertToEntityAttribute(String id) {
        return CUD.valueOd(id);
    }
}
