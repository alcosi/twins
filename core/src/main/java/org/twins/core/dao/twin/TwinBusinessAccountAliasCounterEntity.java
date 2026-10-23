package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.*;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.annotation.DataScope;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.enums.datascope.DataScopeType;

import java.util.UUID;

@Entity
@Data
@Table(name = "twin_business_account_alias_counter")
@DataScope({DataScopeType.DOMAIN_MEMBER, DataScopeType.BUSINESS_ACCOUNT})
public class TwinBusinessAccountAliasCounterEntity {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "business_account_id")
    private UUID businessAccountId;

    @Column(name = "alias_counter")
    private int aliasCounter;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_account_id", insertable = false, updatable = false)
    private BusinessAccountEntity businessAccountSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false, nullable = false)
    private TwinClassEntity twinClassSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private BusinessAccountEntity businessAccount;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassEntity twinClass;
}
