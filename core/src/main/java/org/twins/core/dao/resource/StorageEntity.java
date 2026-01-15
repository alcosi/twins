package org.twins.core.dao.resource;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.twins.core.dao.domain.DomainEntity;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "storage")
public class StorageEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "storager_featurer_id")
    private Integer storageFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "storager_params", columnDefinition = "hstore")
    private HashMap<String, String> storagerParams;

    @Column(name = "description")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at")
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FeaturerEntity storageFeaturer;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    private DomainEntity domain;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "resourceStorage[id:" + id + "]";
            case NORMAL ->
                    "resourceStorage[id:" + id + ", domain:" + domain + ", description:" + description + ", storageFeaturerId:" + storageFeaturerId + "]";
            case DETAILED ->
                    "resourceStorage[id:" + id + ", domain:" + domain + ", description:" + description + ", storageFeaturerId:" + storageFeaturerId + ", params:" + storagerParams.entrySet().stream().filter(it -> it.getValue() != null).map(Map.Entry::getKey).collect(Collectors.joining(",")) + "]";
        };
    }
}
