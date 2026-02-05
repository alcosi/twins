
package org.twins.core.dao.search;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.domain.DomainVersionEntity;
import org.twins.core.domain.versioning.DomainSetting;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@DomainSetting
@Table(name = "twin_search")
public class TwinSearchEntity implements EasyLoggable {
    @Id
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "domain_version_id")
    private UUID domainVersionId;

    @Column(name = "description")
    private String description;

    @Column(name = "permission_id")
    private UUID permissionId;

    @Column(name = "twin_search_alias_id")
    private UUID twinSearchAliasId;

    @Column(name = "force_sorting")
    private boolean forceSorting;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "head_twin_search_id")
    private UUID headTwinSearchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_version_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainVersionEntity domainVersion;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinSearchPredicateEntity, UUID> searchPredicateKit;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinSearchSortEntity, UUID> sortKit;

    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "twinSearch[" + id + "]";
            default:
                return "twinSearch[id:" + id + ", alias:" + twinSearchAliasId + "]";
        }

    }
}
