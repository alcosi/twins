package org.twins.core.dao.factory;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.twins.core.domain.enum_.factory.Action;

@Converter
public class TwinFactoryEraserActionConverter implements AttributeConverter<Action, String> {
    @Override
    public String convertToDatabaseColumn(Action action) {
        return action.getId();
    }

    @Override
    public Action convertToEntityAttribute(String id) {
        return Action.valueOd(id);
    }
}
