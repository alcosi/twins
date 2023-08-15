package org.twins.core.dao.twin;

import lombok.Data;

import jakarta.persistence.*;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.UUID;

@Entity
@Data
@Table(name = "twin_alias_counter")
public class TwinAliasCounterEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "business_account_id")
    private UUID businessAccountId;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "alias_counter")
    private int aliasCounter;

    @ManyToOne
    @JoinColumn(name = "business_account_id", insertable = false, updatable = false)
    private BusinessAccountEntity businessAccount;

    @ManyToOne
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false, nullable = false)
    private TwinClassEntity twinClass;
}
