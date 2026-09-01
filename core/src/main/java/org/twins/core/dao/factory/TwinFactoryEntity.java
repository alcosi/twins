package org.twins.core.dao.factory;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.Type;
import org.hibernate.generator.EventType;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.Identifiable;

import java.sql.Timestamp;
import java.util.HashMap;
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

    @Column(name = "factory_processor_featurer_id")
    private Integer factoryProcessorFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "factory_processor_params", columnDefinition = "hstore")
    private HashMap<String, String> factoryProcessorParams;

    @Deprecated //for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factory_processor_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity factoryProcessorFeaturerSpecOnly;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @CreationTimestamp
    @Column(name = "created_at")
    private Timestamp createdAt;

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

    // Direct join to i18n_translation by raw FK — skips intermediate i18n table.
    // HACK: @Access(PROPERTY) + NOOP getter/setter — see TwinClassFieldEntity.nameI18nTranslationsSpecOnly for explanation
    @Deprecated //for specification only
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Access(AccessType.PROPERTY)
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "i18n_id", referencedColumnName = "name_i18n_id", insertable = false, updatable = false)
    private List<I18nTranslationEntity> nameI18nTranslationsSpecOnly;

    public List<I18nTranslationEntity> getNameI18nTranslationsSpecOnly() {
        return null;
    }

    public void setNameI18nTranslationsSpecOnly(List<I18nTranslationEntity> value) {
        // NOOP
    }

    // Direct join to i18n_translation by raw FK — skips intermediate i18n table
    @Deprecated //for specification only
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Access(AccessType.PROPERTY)
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "i18n_id", referencedColumnName = "description_i18n_id", insertable = false, updatable = false)
    private List<I18nTranslationEntity> descriptionI18nTranslationsSpecOnly;

    public List<I18nTranslationEntity> getDescriptionI18nTranslationsSpecOnly() {
        return null;
    }

    public void setDescriptionI18nTranslationsSpecOnly(List<I18nTranslationEntity> value) {
        // NOOP
    }

    @Transient
    public Integer factoryUsagesCount;

    // Trigger-maintained counter columns: read-only in Java, maintained by AFTER triggers
    // (see V1.4.344.01). insertable=false/updatable=false keeps Hibernate out of the write path
    // (otherwise INSERT sends NULL into a NOT NULL DEFAULT 0 column, and UPDATE clobbers the
    // trigger); @Generated makes Hibernate re-read the row after INSERT/UPDATE so the in-memory
    // value stays correct. Same pattern as TwinEntity permission_schema_id.
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "factory_pipelines_count", insertable = false, updatable = false)
    public Integer factoryPipelinesCount;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "factory_multipliers_count", insertable = false, updatable = false)
    public Integer factoryMultipliersCount;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "factory_branches_count", insertable = false, updatable = false)
    public Integer factoryBranchesCount;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "factory_erasers_count", insertable = false, updatable = false)
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
