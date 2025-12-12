package org.twins.core.dao.idp;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.DynamicUpdate;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "identity_provider_internal_user")
@DynamicUpdate
@Data
@FieldNameConstants
@Accessors(chain = true)
public class IdentityProviderInternalUserEntity implements EasyLoggable {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "last_login_at")
    private Timestamp lastLoginAt;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserEntity user;


    public String easyLog(Level level) {
        return "identityProviderInternalUser[user_id:" + userId + "]";
    }
}
