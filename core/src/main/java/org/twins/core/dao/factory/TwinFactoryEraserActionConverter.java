package org.twins.core.dao.factory;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.twins.core.enums.factory.FactoryEraserAction;

@Converter
public class TwinFactoryEraserActionConverter implements AttributeConverter<FactoryEraserAction, String> {
    @Override
    public String convertToDatabaseColumn(FactoryEraserAction action) {
        return action.getId();
    }

    @Override
    public FactoryEraserAction convertToEntityAttribute(String id) {
        return FactoryEraserAction.valueOd(id);
    }
}
