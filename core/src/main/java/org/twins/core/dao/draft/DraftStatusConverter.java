package org.twins.core.dao.draft;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DraftStatusConverter implements AttributeConverter<DraftEntity.Status, String> {
    @Override
    public String convertToDatabaseColumn(DraftEntity.Status status) {
        return status.getId();
    }

    @Override
    public DraftEntity.Status convertToEntityAttribute(String id) {
        return DraftEntity.Status.valueOd(id);
    }
}
