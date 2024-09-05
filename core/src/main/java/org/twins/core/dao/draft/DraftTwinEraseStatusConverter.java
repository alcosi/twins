package org.twins.core.dao.draft;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DraftTwinEraseStatusConverter implements AttributeConverter<DraftTwinEraseEntity.Status, String> {
    @Override
    public String convertToDatabaseColumn(DraftTwinEraseEntity.Status status) {
        return status.getId();
    }

    @Override
    public DraftTwinEraseEntity.Status convertToEntityAttribute(String id) {
        return DraftTwinEraseEntity.Status.valueOd(id);
    }
}
