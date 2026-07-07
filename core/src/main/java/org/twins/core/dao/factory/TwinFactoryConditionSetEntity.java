package org.twins.core.dao.factory;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.Identifiable;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "twin_factory_condition_set")
public class TwinFactoryConditionSetEntity implements EasyLoggable, Identifiable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "twin_factory_id")
    private UUID twinFactoryId;

    @Column(name = "cachable")
    private Boolean cachable;

    @Transient
    private TwinFactoryEntity twinFactory;

    @Deprecated // for specification only
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

    @Transient
    private Integer inFactoryPipelineUsagesCount;

    @Transient
    private Integer inFactoryPipelineStepUsagesCount;

    @Transient
    private Integer inFactoryMultiplierFilterUsagesCount;

    @Transient
    private Integer inFactoryBranchUsagesCount;

    @Transient
    private Integer inFactoryEraserUsagesCount;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinFactoryConditionEntity, UUID> twinFactoryConditionKit;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinFactoryConditionSet[" + id + "]";
            default -> "twinFactoryConditionSet[id:" + id + ", domainId:" + domainId + "]";
        };
    }
}
