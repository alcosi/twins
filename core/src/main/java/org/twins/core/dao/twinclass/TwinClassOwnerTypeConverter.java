package org.twins.core.dao.twinclass;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TwinClassOwnerTypeConverter implements AttributeConverter<TwinClassEntity.OwnerType, String> {
    @Override
    public String convertToDatabaseColumn(TwinClassEntity.OwnerType ownerType) {
        return ownerType.getId();
    }

    @Override
    public TwinClassEntity.OwnerType convertToEntityAttribute(String id) {
        return TwinClassEntity.OwnerType.valueOd(id);
    }
}
