package org.twins.core.dao;

import io.hypersistence.utils.hibernate.type.array.UUIDArrayType;
import org.hibernate.query.TypedParameterValue;

import java.util.Collection;
import java.util.UUID;

public class TypedParameterTwins {
    public static TypedParameterValue<UUID[]> uuidArray(Collection<UUID> uuids) {
        return new TypedParameterValue(
                UUIDArrayType.INSTANCE,
                uuids != null ? uuids.toArray() : null
        );
    }
}
