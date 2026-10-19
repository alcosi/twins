
package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.cambium.common.util.UuidUtils;
import org.twins.core.enums.twin.TwinAliasType;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_alias")
public class TwinAliasEntity {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

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

    @Column(name = "business_account_id")
    private UUID businessAccountId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "archived")
    private boolean archived;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = true)
    private TwinEntity twinSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinEntity twin;
}
