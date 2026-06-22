package org.twins.core.dao.factory;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.UuidUtils;
import org.hibernate.annotations.CreationTimestamp;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.Identifiable;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
@Entity
@Table(name = "twin_factory")
public class TwinFactoryEntity implements EasyLoggable, Identifiable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "key")
    private String key;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "name_i18n_id")
    private UUID nameI18NId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18NId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Timestamp createdAt;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false)
    private UserEntity createdByUser;

    // Direct join to i18n_translation by raw FK — skips intermediate i18n table
    @Deprecated //for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "i18n_id", referencedColumnName = "name_i18n_id", insertable = false, updatable = false)
    private List<I18nTranslationEntity> nameI18nTranslationsSpecOnly;

    // Direct join to i18n_translation by raw FK — skips intermediate i18n table
    @Deprecated //for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "i18n_id", referencedColumnName = "description_i18n_id", insertable = false, updatable = false)
    private List<I18nTranslationEntity> descriptionI18nTranslationsSpecOnly;

    @Transient
    public Integer factoryUsagesCount;

    @Transient
    public Integer factoryPipelinesCount;

    @Transient
    public Integer factoryMultipliersCount;

    @Transient
    public Integer factoryBranchesCount;

    @Transient
    public Integer factoryErasersCount;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinFactoryMultiplierEntity, UUID> twinFactoryMultiplierKit;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinFactoryPipelineEntity, UUID> twinFactoryPipelineKit;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinFactoryBranchEntity, UUID> twinFactoryBranchKit;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinFactoryEraserEntity, UUID> twinFactoryEraserKit;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinFactoryTriggerEntity, UUID> twinFactoryTriggerKit;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinFactoryConditionSetEntity, UUID> twinFactoryConditionSetKit;

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinFactory[" + id + "]";
            default -> "twinFactory[id:" + id + ", key:" + key + "]";
        };
    }

}
