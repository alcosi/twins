package org.twins.core.dao.twin;

import lombok.Data;

import jakarta.persistence.*;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Table(name = "twin_alias")
public class TwinAliasEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "business_account_id")
    private UUID businessAccountId;

    @Column(name = "alias")
    private String alias;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twin;

    @ManyToOne
    @JoinColumn(name = "business_account_id", insertable = false, updatable = false)
    private BusinessAccountEntity businessAccount;
}
