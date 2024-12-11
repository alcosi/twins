package org.twins.core.dao.businessaccount;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.twins.core.dao.user.UserGroupEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "business_account")
@DynamicUpdate
public class BusinessAccountEntity implements EasyLoggable {
    @Id
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "owner_user_group_id")
    private UUID ownerUserGroupId;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Timestamp createdAt;

//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "owner_user_group_id", insertable = false, updatable = false)
//    private UserGroupEntity ownerUserGroup;

    public String easyLog(Level level) {
        return "businessAccount[id:" + id + "]";
    }
}
