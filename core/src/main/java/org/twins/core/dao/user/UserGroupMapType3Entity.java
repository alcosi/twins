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
@Table(name = "user_group_map_type3")
@FieldNameConstants
public class UserGroupMapType3Entity implements EasyLoggable, UserGroupMap {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "user_group_id")
    private UUID userGroupId;

    @Column(name = "domain_id")
    private UUID domainId;

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
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    private DomainEntity domain;

    public String easyLog(Level level)  {
        return switch (level) {
            case SHORT -> "userGroupMapType3[id:" + id + "]";
            default ->  "userGroupMapType3[id:" + id + ", userGroupId:" + userGroupId + ", userId:" + userId + ", domainId:" + domainId + "]";
        };
    }
}
