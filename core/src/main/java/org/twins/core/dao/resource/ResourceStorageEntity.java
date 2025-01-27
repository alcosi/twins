package org.twins.core.dao.resource;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "resource_storage")
public class ResourceStorageEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "storage_featurer_id")
    private UUID storageFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "storage_params", columnDefinition = "hstore")
    private HashMap<String, String> storageParams;

    @Column(name = "description")
    private String description;
    @CreationTimestamp
    @Column(name = "created_at")
    private Timestamp createdAt;
    @CreationTimestamp
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT ->
                    "resourceStorage[id:" + id + ", description:" + description + ", storageFeaturerId:" + storageFeaturerId + "]";
            case NORMAL, DETAILED ->
                    "resourceStorage[id:" + id + ", description:" + description + ", storageFeaturerId:" + storageFeaturerId + ", params:" + storageParams.entrySet().stream().filter(it -> it.getValue() != null).map(Map.Entry::getKey).collect(Collectors.joining(",")) + "]";
        };
    }
}
