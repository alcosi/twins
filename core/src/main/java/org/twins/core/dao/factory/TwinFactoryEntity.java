package org.twins.core.dao.factory;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.CreationTimestamp;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "twin_factory")
@Accessors(chain = true)
@Data
public class TwinFactoryEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "key")
    private String key;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "name_i18n_id")
    private UUID nameI18NId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18NId;

    @Column(name = "shared")
    private boolean shared; // factory may be used in some transitions or pipelines.

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Timestamp createdAt;

    @ManyToOne
    @EqualsAndHashCode.Exclude
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false)
    private UserEntity createdByUser;

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinFactory[" + id + "]";
            default -> "twinFactory[id:" + id + ", key:" + key + "]";
        };
    }

}
