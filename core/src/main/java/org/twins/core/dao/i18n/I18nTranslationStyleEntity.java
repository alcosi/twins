package org.twins.core.dao.i18n;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.UuidGenerator;

import java.util.Locale;
import java.util.UUID;

@Entity
@Data
@Table(name = "i18n_translation_style")
public class I18nTranslationStyleEntity {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "i18n_id")
    private UUID i18nId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
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
