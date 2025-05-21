package org.twins.core.dao.idp;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.featurer.annotations.FeaturerList;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.twins.core.featurer.identityprovider.connector.IdentityProviderConnector;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.UUID;

@Entity
@Table(name = "identity_provider")
@DynamicUpdate
@Data
@FieldNameConstants
@Accessors(chain = true)
public class IdentityProviderEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "identity_provider_status_id")
    @Enumerated(EnumType.STRING)
    private IdentityProviderStatus status;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "identity_provider_connector_featurer_id")
    private Integer identityProviderConnectorFeaturerId;

    @FeaturerList(type = IdentityProviderConnector.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "identity_provider_connector_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity identityProviderConnectorFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "identity_provider_connector_params", columnDefinition = "hstore")
    private HashMap<String, String> identityProviderConnectorParams;

    public String easyLog(Level level) {
        return "identity_provider[id:" + id + "]";
    }

    @Getter
    public enum IdentityProviderStatus {
        ACTIVE,
        DISABLED
    }
}
