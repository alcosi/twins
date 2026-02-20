package org.twins.core.dao.user;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.usergroup.UserGroupMapEntity;
import org.twins.core.enums.user.UserStatus;

import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

import static org.twins.core.service.user.UserService.maskEmail;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "user")
@FieldNameConstants
public class UserEntity implements EasyLoggable {
    @Id
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @PrePersist
    @PreUpdate
    public void normalizeEmail() {
        if (email != null)
            email = email.toLowerCase();
    }

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "user_status_id")
    @Enumerated(EnumType.STRING)
    private UserStatus userStatusId;

    @Deprecated //for specification only
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<UserGroupMapEntity> userGroupMaps;

    @Deprecated //for specification only
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<SpaceRoleUserEntity> spaceRoleUsers;

    @Transient
    private Kit<UserGroupEntity, UUID> userGroups;

    @Transient
    private Set<UUID> permissions;

    @Transient
    private UUID detectedPermissionSchemaId;

    public String easyLog(Level level) {
        return "user[id:" + id + ", email:" + maskEmail(email) + "]";
    }
}

