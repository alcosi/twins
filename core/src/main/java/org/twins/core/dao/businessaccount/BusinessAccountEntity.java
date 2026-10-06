package org.twins.core.dao.businessaccount;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.DynamicUpdate;
import org.twins.core.domain.Identifiable;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "business_account")
@DynamicUpdate
public class BusinessAccountEntity implements EasyLoggable, Identifiable<UUID> {
    @Id
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "owner_user_group_id")
    private UUID ownerUserGroupId;

    @Column(name = "created_at")
    private Timestamp createdAt;

//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "owner_user_group_id", insertable = false, updatable = false)
//    private UserGroupEntity ownerUserGroup;

    public String easyLog(Level level) {
        return "businessAccount[id:" + id + "]";
    }
}
