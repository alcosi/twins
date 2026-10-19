package org.twins.core.dao.validator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;

import java.util.HashMap;
import java.util.UUID;

@Data
@Entity
@Table(name = "twin_validator")
@Accessors(chain = true)
@FieldNameConstants
public class TwinValidatorEntity implements ContainsTwinValidatorSet, EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_validator_set_id")
    private UUID twinValidatorSetId;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_validator_set_id", insertable = false, updatable = false)
    private TwinValidatorSetEntity twinValidatorSetSpecOnly;

    @Column(name = "twin_validator_featurer_id")
    private Integer twinValidatorFeaturerId;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_validator_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity twinValidatorFeaturerSpecOnly;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "twin_validator_params", columnDefinition = "hstore")
    private HashMap<String, String> twinValidatorParams;

    @Column(name = "invert")
    private Boolean invert;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "description")
    private String description;

    @Column(name = "`order`")
    @Basic
    private Integer order;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinValidatorEntity, UUID> twinValidatorKit;

    @Transient
    @EqualsAndHashCode.Exclude
    private TwinValidatorSetEntity twinValidatorSet;

    /**
     * Null-safe primitive view of {@link #invert} (null and false both yield false).
     */
    @Transient
    @JsonIgnore
    public boolean isInvert() {
        return Boolean.TRUE.equals(invert);
    }

    /**
     * Null-safe primitive view of {@link #active} (null yields false — i.e. treated as inactive).
     */
    @Transient
    @JsonIgnore
    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinValidatorEntity[" + id + "]";
            case NORMAL -> "twinValidatorEntity[id:" + id + ", twinValidatorSetId:" + twinValidatorSetId + "]";
            default ->
                    "twinValidatorEntity[id:" + id + ", twinValidatorSetId:" + twinValidatorSetId + ", twinValidatorFeaturerId:" + twinValidatorFeaturerId + "]";
        };
    }

}
