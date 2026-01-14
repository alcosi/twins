package org.twins.core.dao.idp;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UuidGenerator;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "identity_provider_internal_token")
@DynamicUpdate
@Data
@FieldNameConstants
@Accessors(chain = true)
public class IdentityProviderInternalTokenEntity implements EasyLoggable {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "active_business_account_id")
    private UUID activeBusinessAccountId;

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "access_expires_at")
    private Timestamp accessExpiresAt;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "refresh_expires_at")
    private Timestamp refreshExpiresAt;

    @Column(name = "finger_print")
    private String fingerPrint;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "revoked")
    private boolean revoked;

    @Column(name = "revoked_at")
    private Timestamp revokedAt;

    public String easyLog(Level level) {
        return "identityProviderInternalToken[id:" + id + "]";
    }
}
