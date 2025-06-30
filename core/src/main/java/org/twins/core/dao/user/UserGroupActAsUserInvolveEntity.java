package org.twins.core.dao.user;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.domain.DomainEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "user_group_act_as_user_involve")
@FieldNameConstants
public class UserGroupActAsUserInvolveEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "machine_user_id")
    private UUID machineUserId;

    @Column(name = "involve_in_user_group_id")
    private UUID involveInUserGroupId;

    @Column(name = "added_at")
    private Timestamp addedAt;

    @Column(name = "added_by_user_id")
    private UUID addedByUserId;

    @ManyToOne
    @JoinColumn(name = "added_by_user_id", insertable = false, updatable = false)
    private UserEntity addedByUser;

    @ManyToOne
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    private DomainEntity domain;

    @ManyToOne
    @JoinColumn(name = "involve_in_user_group_id", insertable = false, updatable = false, nullable = false)
    private UserGroupEntity involveInUserGroup;

    public String easyLog(Level level)  {
        return switch (level) {
            case SHORT -> "userGroupActAsUserInvolve[id:" + id + "]";
            default ->  "userGroupActAsUserInvolve[id:" + id + ", involveInUserGroupId:" + involveInUserGroupId + ", machineUserId:" + machineUserId + ", domainId:" + domainId + "]";
        };
    }
}
