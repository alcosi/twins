package org.twins.core.dao.twinclass;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.twins.core.enums.twinclass.OwnerType;

@Converter
public class TwinClassOwnerTypeConverter implements AttributeConverter<OwnerType, String> {
    @Override
    public String convertToDatabaseColumn(OwnerType ownerType) {
        return ownerType.getId();
    }

    @Override
    public OwnerType convertToEntityAttribute(String id) {
        return OwnerType.valueOd(id);
    }
}
