package org.twins.core.dao.datalist;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.twins.core.enums.datalist.DataListStatus;

@Converter
public class DataListOptionStatusConverter implements AttributeConverter<DataListStatus, String> {
    @Override
    public String convertToDatabaseColumn(DataListStatus status) {
        return status.getId();
    }

    @Override
    public DataListStatus convertToEntityAttribute(String id) {
        return DataListStatus.valueOd(id);
    }
}
