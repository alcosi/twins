package org.twins.core.dao.user;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.DynamicUpdate;
import org.twins.core.dao.idp.IdentityProviderEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "user_email_verification")
@DynamicUpdate
@Data
@FieldNameConstants
@Accessors(chain = true)
public class UserEmailVerificationEntity implements EasyLoggable {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "email")
    private String email;

    @Column(name = "identity_provider_id")
    private UUID identityProviderId;

    @Column(name = "verification_code_idp")
    private String verificationCodeIDP;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "identity_provider_id", insertable = false, updatable = false)
    private IdentityProviderEntity identityProvider;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserEntity user;


    public String easyLog(Level level) {
        return "userEmailVerification[user_id:" + userId + "]";
    }
}
