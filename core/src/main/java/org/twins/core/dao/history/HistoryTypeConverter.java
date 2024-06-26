package org.twins.core.dao.history;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class HistoryTypeConverter implements AttributeConverter<HistoryType, String> {
    @Override
    public String convertToDatabaseColumn(HistoryType historyType) {
        return historyType.getId();
    }

    @Override
    public HistoryType convertToEntityAttribute(String id) {
        return HistoryType.valueOd(id);
    }
}
