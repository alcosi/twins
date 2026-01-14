package org.twins.core.dao.email;

import com.github.f4b6a3.uuid.UuidCreator;
import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
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
@Table(name = "email_sender")
@DynamicUpdate
@Data
@FieldNameConstants
@Accessors(chain = true)
public class EmailSenderEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UuidCreator.getTimeOrderedEpoch();
        }
    }

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "src_email")
    private String srcEmail;

    @Column(name = "active")
    private boolean active;

    @Column(name = "emailer_featurer_id")
    private Integer emailerFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "emailer_params", columnDefinition = "hstore")
    private HashMap<String, String> emailerParams;

    @Column(name = "created_at")
    private Timestamp createdAt;

    public String easyLog(Level level) {
        return "emailSender[id:" + id + "]";
    }
}
