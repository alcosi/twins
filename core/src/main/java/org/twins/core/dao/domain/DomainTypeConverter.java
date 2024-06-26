package org.twins.core.dao.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DomainTypeConverter implements AttributeConverter<DomainType, String> {
    @Override
    public String convertToDatabaseColumn(DomainType domainType) {
        return domainType.getId();
    }

    @Override
    public DomainType convertToEntityAttribute(String id) {
        return DomainType.valueOd(id);
    }
}
