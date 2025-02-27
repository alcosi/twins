package org.twins.core.dao.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.twins.core.i18n.dao.I18nLocaleEntity;
import org.twins.core.i18n.dao.LocaleConverter;

import java.util.Locale;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "domain_locale")
public class DomainLocaleEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "i18n_locale_id")
    @Convert(converter = LocaleConverter.class)
    private Locale locale;

    @Column(name = "icon")
    private String icon;

    @Column(name = "active")
    private boolean active;

    @ManyToOne
    @JoinColumn(name = "i18n_locale_id", insertable = false, updatable = false, nullable = false)
    private I18nLocaleEntity i18nLocale;

    @Override
    public String easyLog(Level level) {
        return "domainLocale[id:" + id + ", domainId:" + domainId + ", locale:" + locale + "]";
    }
}
