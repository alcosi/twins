package org.cambium.i18n.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.kit.Kit;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "i18n")
@FieldNameConstants
public class I18nEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    @Column(name = "id")
    private UUID id;

    @Basic
    @Column(name = "name")
    private String name;

    @Column(name = "i18n_type_id")
    @Convert(converter = I18nTypeConverter.class)
    private I18nType type;

    @Basic
    @Column(name = "key")
    private String key;

    @OneToMany(fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "i18n_id", insertable = false, updatable = false)
    @JsonIgnore
    private List<I18nTranslationEntity> translations;

    @Transient
    private Kit<I18nTranslationEntity, Locale> translationsKit;

    @Transient
    public I18nEntity addTranslation(I18nTranslationEntity translationEntity) {
        if (translationsKit == null)
            translationsKit = new Kit<>(I18nTranslationEntity::getLocale);
        translationsKit.add(translationEntity);
        return this;
    }

    @Transient
    private Kit<I18nTranslationBinEntity, Locale> translationsBin;

    @Override
    public String toString() {
        return "I18NEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
