package org.twins.core.dao.permission;

import jakarta.persistence.*;
import lombok.Data;
import org.cambium.common.EasyLoggableImpl;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.UUID;

@Entity
@Data
@Table(name = "permission_group")
public class PermissionGroupEntity extends EasyLoggableImpl {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "key")
    private String key;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    private DomainEntity domain;

    @ManyToOne
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false)
    private TwinClassEntity twinClass;

    @Override
    public String easyLog(Level level) {
        return "permissionGroup[id:" + id + ", key:" + key + "]";
    }
}
