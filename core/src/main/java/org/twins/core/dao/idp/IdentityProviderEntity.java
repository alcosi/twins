package org.twins.core.dao.idp;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;

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
    @UuidGenerator(style = UuidGenerator.Style.TIME)
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

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "identity_provider_connector_params", columnDefinition = "hstore")
    private HashMap<String, String> identityProviderConnectorParams;

    @Column(name = "trustor_featurer_id")
    private Integer trustorFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "trustor_params", columnDefinition = "hstore")
    private HashMap<String, String> trustorParams;

    public String easyLog(Level level) {
        return "identity_provider[id:" + id + "]";
    }

    @Getter
    public enum IdentityProviderStatus {
        ACTIVE,
        DISABLED
    }
}
