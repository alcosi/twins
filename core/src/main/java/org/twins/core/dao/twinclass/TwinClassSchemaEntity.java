package org.twins.core.dao.twinclass;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(fluent = true)
@Table(name = "twin_class_schema")
public class TwinClassSchemaEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "created_at")
    private Timestamp createdAt;

//    @ManyToOne
//    @JoinColumn(name = "domain_id", insertable = false, updatable = false, nullable = false)
//    private DomainEntity domain;
//
//    @ManyToOne
//    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false, nullable = false)
//    private UserEntity createdByUser;
}
