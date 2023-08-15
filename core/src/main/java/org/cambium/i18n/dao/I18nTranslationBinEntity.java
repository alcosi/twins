package org.cambium.i18n.dao;

import lombok.Data;

import jakarta.persistence.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.*;

@Entity
@Data
@Table(name = "i18n_translation_bin")
public class I18nTranslationBinEntity {
    @Id
    @Column(name = "i18n_id")
    private UUID i18nId;

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
