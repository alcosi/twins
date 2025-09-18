package org.twins.core.dao.datalist;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.twins.core.domain.enum_.datalist.Status;

@Converter
public class DataListOptionStatusConverter implements AttributeConverter<Status, String> {
    @Override
    public String convertToDatabaseColumn(Status status) {
        return status.getId();
    }

    @Override
    public Status convertToEntityAttribute(String id) {
        return Status.valueOd(id);
    }
}
