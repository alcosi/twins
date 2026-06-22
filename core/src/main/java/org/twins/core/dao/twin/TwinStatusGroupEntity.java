package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.*;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.domain.DomainEntity;

import java.util.UUID;

@Entity
@Data
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

    @Column(name = "key")
    private String key;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    private DomainEntity domainSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainEntity domain;
}
