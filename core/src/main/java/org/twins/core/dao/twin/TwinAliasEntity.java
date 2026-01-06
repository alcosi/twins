
package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.twins.core.enums.twin.TwinAliasType;

import java.sql.Timestamp;
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

    @Column(name = "business_account_id")
    private UUID businessAccountId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "archived")
    private boolean archived;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = true)
    private TwinEntity twin;
}
