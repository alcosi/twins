package org.twins.core.dao.user;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Table(name = "user_group_map")
@IdClass(UserGroupMapEntity.PK.class)
public class UserGroupMapEntity {
    @Id
    @Column(name = "user_group_id")
    private UUID userGroupId;

    @Id
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

    @Data
    public static class PK implements Serializable {
        @Column(name = "user_group_id")
        private UUID userGroupId;

        @Column(name = "user_id")
        private UUID userId;
    }
}
