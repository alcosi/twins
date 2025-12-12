package org.twins.core.dao.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.businessaccount.BusinessAccountUserEntity;
import org.twins.core.dao.i18n.LocaleConverter;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;


@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "domain_user")
public class DomainUserEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "i18n_locale_id")
    @Convert(converter = LocaleConverter.class)
    private Locale i18nLocaleId;

    @Column(name = "last_active_business_account_id")
    private UUID lastActiveBusinessAccountId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    private DomainEntity domain;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserEntity user;

//    needed for specification
    @Deprecated
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", referencedColumnName = "domain_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<DomainBusinessAccountEntity> domainBusinessAccountsByDomainId;

//    needed for specification
    @Deprecated
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<BusinessAccountUserEntity> businessAccountUsersByUserId;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<BusinessAccountUserEntity, UUID> businessAccountUserKit;

    public String easyLog(Level level) {
        return "domainUser[id:" + id + ", domainId:" + domainId + ", userId:" + userId + "]";
    }
}
