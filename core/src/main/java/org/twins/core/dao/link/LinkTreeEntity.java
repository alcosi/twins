package org.twins.core.dao.link;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Table(name = "link_tree")
public class LinkTreeEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "name")
    private String name;

    @Column(name = "root_twin_class_id")
    private UUID rootTwinClassId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Timestamp createdAt;

    @ManyToOne
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    private DomainEntity domain;

    @ManyToOne
    @JoinColumn(name = "root_twin_class_id", insertable = false, updatable = false, nullable = false)
    private TwinClassEntity twinClassByRootTwinClassId;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity createdByUser;
}
