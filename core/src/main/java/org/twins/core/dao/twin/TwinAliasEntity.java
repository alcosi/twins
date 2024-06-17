
package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.user.UserEntity;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_alias")
public class TwinAliasEntity {

    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "alias_value")
    private String alias;

    @Column(name = "twin_alias_type_id")
    @Enumerated(EnumType.STRING)
    private TwinAliasType aliasTypeId;

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "businesw_account_id")
    private UUID businessAccountId;

    @JoinColumn(name = "business_account_id")
    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = true)
    private TwinEntity twin;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false, nullable = true)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "domain_id", insertable = false, updatable = false, nullable = true)
    private DomainEntity domain;

    @ManyToOne
    @JoinColumn(name = "business_account_id", insertable = false, updatable = false, nullable = true)
    private BusinessAccountEntity businessAccount;
}
