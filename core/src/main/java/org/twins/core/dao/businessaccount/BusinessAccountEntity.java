package org.twins.core.dao.businessaccount;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicUpdate;
import org.twins.core.dao.user.UserGroupEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(fluent = true)
@Table(name = "business_account")
@DynamicUpdate
public class BusinessAccountEntity {
    @Id
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "owner_user_group_id")
    private UUID ownerUserGroupId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_user_group_id", insertable = false, updatable = false)
    private UserGroupEntity ownerUserGroup;
}