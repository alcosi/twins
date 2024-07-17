package org.twins.core.dao.twin;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TwinEraserReasonConverter implements AttributeConverter<TwinEraserTransactionScopeEntity.Reason, String> {
    @Override
    public String convertToDatabaseColumn(TwinEraserTransactionScopeEntity.Reason action) {
        return action.getId();
    }

    @Override
    public TwinEraserTransactionScopeEntity.Reason convertToEntityAttribute(String id) {
        return TwinEraserTransactionScopeEntity.Reason.valueOd(id);
    }
}
