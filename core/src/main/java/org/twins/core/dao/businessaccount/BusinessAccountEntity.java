package org.twins.core.dao.businessaccount;

import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.*;
import org.twins.core.dao.user.UserGroupEntity;

import java.util.UUID;

@Entity
@Data
@Table(name = "business_account")
@DynamicUpdate
public class BusinessAccountEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "owner_user_group_id")
    private UUID ownerUserGroupId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_user_group_id", insertable = false, updatable = false)
    private UserGroupEntity ownerUserGroup;
}