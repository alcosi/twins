package org.twins.core.dao.view;

import lombok.Data;

import jakarta.persistence.*;
import org.twins.core.dao.AccessRule;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.UUID;

@Entity
@Data
@Table(name = "view_access")
public class ViewAccessEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "view_id")
    private UUID viewId;

    @Column(name = "access_rule")
    @Enumerated(EnumType.STRING)
    private AccessRule accessRule;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @ManyToOne
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false, nullable = false)
    private TwinClassEntity twinClass;
}
