package org.twins.core.dao.domain;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.UuidGenerator;
import org.twins.core.dao.i18n.I18nLocaleEntity;
import org.twins.core.dao.i18n.LocaleConverter;

import java.util.Locale;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "domain_locale")
public class DomainLocaleEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UuidCreator.getTimeOrderedEpoch();
        }
    }

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "i18n_locale_id")
    @Convert(converter = LocaleConverter.class)
    private Locale locale;

    @Column(name = "icon")
    private String icon;

    @Column(name = "active")
    private boolean active;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "i18n_locale_id", insertable = false, updatable = false, nullable = false)
    private I18nLocaleEntity i18nLocale;

    @Override
    public String easyLog(Level level) {
        return "domainLocale[id:" + id + ", domainId:" + domainId + ", locale:" + locale + "]";
    }
}
