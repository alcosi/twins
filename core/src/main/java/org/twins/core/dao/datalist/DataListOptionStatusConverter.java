package org.twins.core.dao.datalist;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DataListOptionStatusConverter implements AttributeConverter<DataListOptionEntity.Status, String> {
    @Override
    public String convertToDatabaseColumn(DataListOptionEntity.Status status) {
        return status.getId();
    }

    @Override
    public DataListOptionEntity.Status convertToEntityAttribute(String id) {
        return DataListOptionEntity.Status.valueOd(id);
    }
}
