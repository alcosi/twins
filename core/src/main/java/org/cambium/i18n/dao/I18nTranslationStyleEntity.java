package org.cambium.i18n.dao;

import lombok.Data;

import jakarta.persistence.*;

import java.util.Locale;
import java.util.UUID;

@Entity
@Data
@Table(name = "i18n_translation_style")
public class I18nTranslationStyleEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "i18n_id")
    private UUID i18nId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "i18n_id", insertable = false, updatable = false)
    private I18nEntity i18n;

    @Column(name = "locale")
    @Convert(converter = LocaleConverter.class)
    private Locale locale;

    @Column(name = "start_index")
    private Integer startIndex;

    @Column(name = "end_index")
    private Integer endIndex;

    @Column(name = "color")
    private String color;

    @Column(name = "size")
    private String size;

    @Column(name = "link")
    private String link;
}
