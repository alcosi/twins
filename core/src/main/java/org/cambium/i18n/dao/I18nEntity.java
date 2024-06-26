package org.cambium.i18n.dao;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.kit.Kit;

import java.util.Locale;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "i18n")
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

    @Transient
    private Kit<I18nTranslationEntity, Locale> translations;

    @Transient
    public I18nEntity addTranslation(I18nTranslationEntity translationEntity) {
        if (translations == null)
            translations = new Kit<>(I18nTranslationEntity::getLocale);
        translations.add(translationEntity);
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
