package org.twins.core.dao.history;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class HistoryTypeStatusConverter implements AttributeConverter<HistoryTypeStatus, String> {
    @Override
    public String convertToDatabaseColumn(HistoryTypeStatus historyTypeStatus) {
        return historyTypeStatus.getId();
    }

    @Override
    public HistoryTypeStatus convertToEntityAttribute(String id) {
        return HistoryTypeStatus.valueOd(id);
    }
}
