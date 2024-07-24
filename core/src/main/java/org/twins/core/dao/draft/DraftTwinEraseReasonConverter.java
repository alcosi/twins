package org.twins.core.dao.draft;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DraftTwinEraseReasonConverter implements AttributeConverter<DraftTwinEraseEntity.Reason, String> {
    @Override
    public String convertToDatabaseColumn(DraftTwinEraseEntity.Reason reason) {
        return reason.getId();
    }

    @Override
    public DraftTwinEraseEntity.Reason convertToEntityAttribute(String id) {
        return DraftTwinEraseEntity.Reason.valueOd(id);
    }
}
