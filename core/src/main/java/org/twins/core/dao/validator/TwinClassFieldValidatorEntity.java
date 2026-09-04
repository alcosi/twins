package org.twins.core.dao.validator;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.Identifiable;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "twin_class_field_validator")
@Accessors(chain = true)
@FieldNameConstants
public class TwinClassFieldValidatorEntity implements EasyLoggable, Identifiable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_class_field_id", nullable = false)
    private UUID twinClassFieldId;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_field_id", insertable = false, updatable = false)
    private TwinClassFieldEntity twinClassFieldSpecOnly;

    @Column(name = "field_validator_featurer_id", nullable = false)
    private Integer fieldValidatorFeaturerId;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_validator_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity fieldValidatorFeaturerSpecOnly;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "field_validator_params", columnDefinition = "hstore")
    private HashMap<String, String> fieldValidatorParams;

    @Column(name = "be_validation_error_i18n_id")
    private UUID beValidationErrorI18nId;

    // Direct join to i18n_translation by raw FK — skips intermediate i18n table.
    // HACK: @Access(PROPERTY) + NOOP getter/setter — see entity_code_convention.md §6.6
    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Access(AccessType.PROPERTY)
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "i18n_id",
            referencedColumnName = "be_validation_error_i18n_id",
            insertable = false,
            updatable = false
    )
    private List<I18nTranslationEntity> beValidationErrorI18nTranslationsSpecOnly;

    public List<I18nTranslationEntity> getBeValidationErrorI18nTranslationsSpecOnly() {
        return null;
    }

    public void setBeValidationErrorI18nTranslationsSpecOnly(List<I18nTranslationEntity> value) {
        // NOOP — never store PersistentBag, so Hibernate flush visitor sees null
    }

    @Transient
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TwinClassFieldEntity twinClassField;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinClassFieldValidator[" + id + "]";
            case NORMAL -> "twinClassFieldValidator[id:" + id + ", twinClassFieldId:" + twinClassFieldId + "]";
            default -> "twinClassFieldValidator[id:" + id
                    + ", twinClassFieldId:" + twinClassFieldId
                    + ", fieldValidatorFeaturerId:" + fieldValidatorFeaturerId
                    + ", fieldValidatorParams:" + fieldValidatorParams + "]";
        };
    }
}
