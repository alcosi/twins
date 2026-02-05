package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.domain.DomainVersionEntity;
import org.twins.core.domain.versioning.DomainSetting;

import java.util.UUID;

@Entity
@Data
@DomainSetting
@Table(name = "twin_status_group")
public class TwinStatusGroupEntity {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "domain_version_id")
    private UUID domainVersionId;

    @Column(name = "key")
    private String key;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    private DomainEntity domain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_version_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainVersionEntity domainVersion;
}
