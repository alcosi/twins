package org.twins.core.dao.notification.email;

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
@Table(name = "email_sender")
@DynamicUpdate
@Data
@FieldNameConstants
@Accessors(chain = true)
public class EmailSenderEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "owner_domain_id")
    private UUID domainId;

    @Column(name = "email_sender_status_id")
    @Enumerated(EnumType.STRING)
    private EmailSenderStatus status;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "emailer_featurer_id")
    private Integer emailerFeaturerId;

    @FeaturerList(type = IdentityProviderConnector.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "emailer_params", insertable = false, updatable = false)
    private FeaturerEntity emailerFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "emailer_params", columnDefinition = "hstore")
    private HashMap<String, String> emailerParams;

    public String easyLog(Level level) {
        return "emailSender[id:" + id + "]";
    }

    @Getter
    public enum EmailSenderStatus {
        ACTIVE,
        DISABLED
    }
}
