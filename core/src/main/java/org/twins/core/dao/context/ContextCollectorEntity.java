package org.twins.core.dao.context;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Table(name = "context_collector")
@DynamicUpdate
@Data
@FieldNameConstants
@Accessors(chain = true)
public class ContextCollectorEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "context_id")
    private UUID contextId;

    @Column(name = "context_collector_featurer_id", nullable = false)
    private Integer contextCollectorFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "context_collector_params", columnDefinition = "hstore")
    private HashMap<String, String> contextCollectorParams;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "context_id", insertable = false, updatable = false)
    private ContextEntity contextSpecOnly;

    public String easyLog(Level level) {
        return "contextCollector[id:" + id + "]";
    }
}

