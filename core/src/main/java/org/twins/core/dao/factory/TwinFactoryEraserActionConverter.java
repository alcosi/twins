package org.twins.core.dao.factory;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TwinFactoryEraserActionConverter implements AttributeConverter<TwinFactoryEraser.Action, String> {
    @Override
    public String convertToDatabaseColumn(TwinFactoryEraser.Action action) {
        return action.getId();
    }

    @Override
    public TwinFactoryEraser.Action convertToEntityAttribute(String id) {
        return TwinFactoryEraser.Action.valueOd(id);
    }
}
