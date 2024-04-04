package org.cambium.i18n.dao;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Entity
@Data
@Table(name = "i18n_locale")
@FieldNameConstants
public class I18nLocaleEntity {
    @Id
    @Column(name = "locale")
    private String locale;

    @Basic
    @Column(name = "name")
    private String name;

    @Basic
    @Column(name = "active")
    private boolean active;

    @Column(name = "native_name")
    private String nativeName;

    @Column(name = "icon")
    private String icon;
}
