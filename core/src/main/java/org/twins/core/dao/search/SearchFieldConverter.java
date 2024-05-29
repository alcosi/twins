package org.twins.core.dao.search;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class SearchFieldConverter implements AttributeConverter<SearchField, String> {
    @Override
    public String convertToDatabaseColumn(SearchField searchField) {
        return searchField.getId();
    }

    @Override
    public SearchField convertToEntityAttribute(String id) {
        return SearchField.valueOd(id);
    }
}
