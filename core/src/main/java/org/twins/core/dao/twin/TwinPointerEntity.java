package org.twins.core.dao.twin;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.hibernate.annotations.Type;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "twin_pointer")
public class TwinPointerEntity implements EasyLoggable {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "pointer_featurer_id")
    private Integer pointerFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "pointer_params", columnDefinition = "hstore")
    private HashMap<String, String> pointerParams;

    @Column(name = "name")
    private String name;

    // TWINS-875: when true, a pointer resolution failure (e.g. POINTER_NON_SINGLE) is swallowed
    // (log.warn + cached null) instead of propagating, so one anomalous twin cannot roll back the
    // whole recompute batch. Default false = strict fail-fast (previous behaviour).
    @Column(name = "optional")
    private Boolean optional;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false)
    private TwinClassEntity twinClass;

    @Deprecated //for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false)
    private UserEntity createdByUserSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity createdByUser;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "twinPointer[" + id + "]";
            default:
                return "twinPointer[id:" + id + ", twinClassId:" + twinClassId + ", domainId:" + domainId + "]";
        }
    }
}
