package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.UUID;

@Entity
@Data
@Table(name = "twin_business_account_alias_counter")
public class TwinBusinessAccountAliasCounterEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "business_account_id")
    private UUID businessAccountId;

    @Column(name = "alias_counter")
    private int aliasCounter;

    @ManyToOne
    @JoinColumn(name = "business_account_id", insertable = false, updatable = false)
    private BusinessAccountEntity businessAccount;

    @ManyToOne
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false, nullable = false)
    private TwinClassEntity twinClass;
}
