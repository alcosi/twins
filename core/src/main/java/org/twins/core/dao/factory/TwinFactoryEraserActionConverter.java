package org.twins.core.dao.factory;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TwinFactoryEraserActionConverter implements AttributeConverter<TwinFactoryEraserEntity.Action, String> {
    @Override
    public String convertToDatabaseColumn(TwinFactoryEraserEntity.Action action) {
        return action.getId();
    }

    @Override
    public TwinFactoryEraserEntity.Action convertToEntityAttribute(String id) {
        return TwinFactoryEraserEntity.Action.valueOd(id);
    }
}
