package org.cambium.i18n.dao;

import lombok.Data;

import jakarta.persistence.*;

import java.util.Locale;

@Entity
@Data
@Table(name = "i18n_locale")
public class I18nLocaleEntity {
    @Id
    @Column(name = "locale")
    @Convert(converter = LocaleConverter.class)
    private Locale locale;

    @Basic
    @Column(name = "name")
    private String name;

    @Basic
    @Column(name = "active")
    private boolean active;
}
