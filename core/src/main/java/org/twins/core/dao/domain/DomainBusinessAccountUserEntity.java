package org.twins.core.dao.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.businessaccount.BusinessAccountUserEntity;
import org.twins.core.dao.user.UserEntity;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_user_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainUserEntity domainUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_business_account_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainBusinessAccountEntity domainBusinessAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_account_user_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private BusinessAccountUserEntity businessAccountUser;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainEntity domain;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "business_account_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private BusinessAccountEntity businessAccount;

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
