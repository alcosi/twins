package org.twins.core.dao.datalist;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;
import org.twins.core.dao.DomainConfigEntity;
import org.twins.core.dao.annotation.DataScope;
import org.twins.core.enums.datascope.DataScopeType;

import java.util.UUID;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@Table(name = "data_list_subset_option")
@FieldNameConstants
@DataScope({DataScopeType.SYSTEM, DataScopeType.DOMAIN})
public class DataListSubsetOptionEntity extends DomainConfigEntity {
    // same namespace as the deterministic backfill in V1.4.381.01 (uuid_generate_v5):
    // independently migrated environments must converge to the same id for the same business key
    private static final UUID ID_NAMESPACE = UUID.fromString("71900000-0000-0000-0000-000000000001");

    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            // uuid v5 (SHA-1 name-based) of the business key pair — identical to uuid_generate_v5 in PG
            id = UuidCreator.getNameBasedSha1(ID_NAMESPACE, dataListSubsetId + ":" + dataListOptionId);
        }
    }

    @Column(name = "data_list_subset_id")
    private UUID dataListSubsetId;

    @Column(name = "data_list_option_id")
    private UUID dataListOptionId;

    @Column(name = "domain_id")
    private UUID domainId;

    // DB-maintained by the versioning triggers (TWINS-719) — Hibernate never writes it
    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "domain_version_id", insertable = false, updatable = false)
    private UUID domainVersionId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_list_subset_id", insertable = false, updatable = false)
    private DataListSubsetEntity dataListSubset;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_list_option_id", insertable = false, updatable = false)
    private DataListOptionEntity dataListOption;
}
