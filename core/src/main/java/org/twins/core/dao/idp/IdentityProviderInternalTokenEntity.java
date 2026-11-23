package org.twins.core.dao.idp;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.hibernate.annotations.DynamicUpdate;
import org.twins.core.dao.annotation.DataScope;
import org.twins.core.enums.datascope.DataScopeType;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "identity_provider_internal_token")
@DynamicUpdate
@Data
@FieldNameConstants
@Accessors(chain = true)
@DataScope(DataScopeType.DOMAIN_MEMBER)
public class IdentityProviderInternalTokenEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "domain_id", nullable = false)
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
