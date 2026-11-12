package org.twins.core.dao.i18n;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dao.annotation.DataScope;
import org.twins.core.enums.datascope.DataScopeType;

@Entity
@Data
@Table(name = "i18n_locale")
@FieldNameConstants
@DataScope(DataScopeType.SYSTEM)
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
