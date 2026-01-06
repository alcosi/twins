package org.twins.core.dao.i18n;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

@Entity
@Data
@Table(name = "i18n_translation_bin")
@IdClass(I18nTranslationBinEntity.PK.class)
public class I18nTranslationBinEntity {
    @Id
    @Column(name = "i18n_id")
    private UUID i18nId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "i18n_id", insertable = false, updatable = false)
    private I18nEntity i18n;

    @Id
    @Column(name = "locale")
    @Convert(converter = LocaleConverter.class)
    private Locale locale;

    @Basic
    @Column(name = "translation")
    private byte[] translation;

    @Transient
    private MultipartFile uploadedFile;

    /**
     * Для отправки строкой по текстовому протоколу
     */
    @Transient
    public String getBase64() {
        return Base64.getEncoder().encodeToString(translation);
    }

    @Data
    protected static class PK implements Serializable {
        @Column(name = "i18n_id")
        private UUID i18nId;

        @Column(name = "locale")
        @Convert(converter = LocaleConverter.class)
        private Locale locale;
    }
}
