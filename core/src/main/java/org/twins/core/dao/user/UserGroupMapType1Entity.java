package org.twins.core.dao.user;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "user_group_map_type1")
@FieldNameConstants
public class UserGroupMapType1Entity implements EasyLoggable, UserGroupMap {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "user_group_id")
    private UUID userGroupId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "added_at")
    private Timestamp addedAt;

    @Column(name = "added_by_user_id")
    private UUID addedByUserId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "user_group_id", insertable = false, updatable = false, nullable = false)
    private UserGroupEntity userGroup;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserEntity user;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "added_by_user_id", insertable = false, updatable = false)
    private UserEntity addedByUser;


    public String easyLog(Level level)  {
        return switch (level) {
            case SHORT -> "userGroupMapType1[id:" + id + "]";
            default ->  "userGroupMapType1[id:" + id + ", userGroupId:" + userGroupId + ", userId:" + userId + "]";
        };
    }
}
