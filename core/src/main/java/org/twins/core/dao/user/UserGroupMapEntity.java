package org.twins.core.dao.user;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "user_group_map")
@FieldNameConstants
public class UserGroupMapEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "user_group_id")
    private UUID userGroupId;

    @Column(name = "business_account_id")
    private UUID businessAccountId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "added_at")
    private Timestamp addedAt;

    @Column(name = "added_by_user_id")
    private UUID addedByUserId;

    @ManyToOne
    @JoinColumn(name = "user_group_id", insertable = false, updatable = false, nullable = false)
    private UserGroupEntity userGroup;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "added_by_user_id", insertable = false, updatable = false)
    private UserEntity addedByUser;

    @ManyToOne
    @JoinColumn(name = "business_account_id", insertable = false, updatable = false)
    private BusinessAccountEntity businessAccount;

    public String easyLog(Level level)  {
        return "userGroupMap[id:" + id + ", userGroupId:" + userGroupId + ", userId:" + userId + "]";
    }
}
