package org.twins.core.dao;

import io.hypersistence.utils.hibernate.type.array.UUIDArrayType;
import org.hibernate.query.TypedParameterValue;
import org.hibernate.type.descriptor.java.UUIDJavaType;
import org.hibernate.type.descriptor.jdbc.UUIDJdbcType;
import org.hibernate.type.internal.BasicTypeImpl;

import java.util.Collection;
import java.util.UUID;

public class TypedParameterTwins {
    public static TypedParameterValue<UUID[]> uuidArray(Collection<UUID> uuids) {
        return new TypedParameterValue(
                UUIDArrayType.INSTANCE,
                uuids != null ? uuids.toArray() : null
        );
    }

    public static TypedParameterValue<UUID> uuidNullable(UUID uuid) {
        return new TypedParameterValue(
                new BasicTypeImpl<>(
                        UUIDJavaType.INSTANCE,
                        UUIDJdbcType.INSTANCE),
                uuid
        );
    }
}
