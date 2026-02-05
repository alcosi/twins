package org.twins.core.dao.trigger;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
@Entity
@Table(name = "twin_trigger")
public class TwinTriggerEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "twin_trigger_featurer_id")
    private Integer twinTriggerFeaturerId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "twin_trigger_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity twinTriggerFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "twin_trigger_param", columnDefinition = "hstore")
    private HashMap<String, String> twinTriggerParam;

    @Column(name = "active")
    private Boolean active;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinTrigger[" + id + "]";
            default -> "twinTrigger[id:" + id + ", domainId:" + domainId + ", active:" + active + "]";
        };
    }
}
