package org.twins.core.dao.permission;

import lombok.Data;

import jakarta.persistence.*;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.UUID;

@Entity
@Data
@Table(name = "permission_group")
public class PermissionGroupEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    private DomainEntity domain;

    @ManyToOne
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false)
    private TwinClassEntity twinClass;
}
