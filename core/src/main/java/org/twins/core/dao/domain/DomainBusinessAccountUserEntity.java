package org.twins.core.dao.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.businessaccount.BusinessAccountUserEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserGroupEntity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "domain_business_account_user")
@IdClass(DomainBusinessAccountUserEntity.Pk.class)
public class DomainBusinessAccountUserEntity implements EasyLoggable {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Id
    @Column(name = "domain_id")
    private UUID domainId;

    @Id
    @Column(name = "business_account_id")
    private UUID businessAccountId;

    @Column(name = "domain_user_id", insertable = false, updatable = false)
    private UUID domainUserId;

    @Column(name = "domain_business_account_id", insertable = false, updatable = false)
    private UUID domainBusinessAccountId;

    @Column(name = "business_account_user_id", insertable = false, updatable = false)
    private UUID businessAccountUserId;

    @Column(name = "last_activity_at")
    private Timestamp lastActivityAt;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_user_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainUserEntity domainUserSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_business_account_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainBusinessAccountEntity domainBusinessAccountSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_account_user_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private BusinessAccountUserEntity businessAccountUserSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity userSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainEntity domainSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_account_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private BusinessAccountEntity businessAccountSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainUserEntity domainUser;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainBusinessAccountEntity domainBusinessAccount;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private BusinessAccountUserEntity businessAccountUser;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity user;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainEntity domain;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private BusinessAccountEntity businessAccount;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<UserGroupEntity, UUID> userGroupKit;

    @Data
    public static class Pk implements Serializable {
        private UUID userId;
        private UUID domainId;
        private UUID businessAccountId;
    }

    public String easyLog(Level level) {
        return "domainBusinessAccountUser[userId:" + userId + ", domainId:" + domainId + ", businessAccountId:" + businessAccountId + "]";
    }
}
