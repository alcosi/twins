package org.cambium.i18n.dao;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
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

    public List<I18nTranslationBinEntity> getTranslationsBin() {
        return translationsBin;
    }

    public void setTranslationsBin(List<I18nTranslationBinEntity> translationsBin) {
        this.translationsBin = translationsBin;
    }

    @Transient
    private List<I18nTranslationEntity> translations;

    @Transient
    public void addTranslation(I18nTranslationEntity translationEntity) {
        if (translations == null)
            translations = new ArrayList<>();
        translations.add(translationEntity);
    }

    @Transient
    private List<I18nTranslationBinEntity> translationsBin;

    @Transient
    public void addTranslationBin(I18nTranslationBinEntity translationtranslationBinEntity) {
        if (translationsBin == null)
            translationsBin = new ArrayList<>();
        translationsBin.add(translationtranslationBinEntity);
    }

    @Override
    public String toString() {
        return "I18NEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
