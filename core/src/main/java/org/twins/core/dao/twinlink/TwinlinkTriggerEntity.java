package org.twins.core.dao.twinlink;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.cambium.featurer.annotations.FeaturerList;

import jakarta.persistence.*;
import org.cambium.featurer.dao.FeaturerEntity;
import org.twins.core.featurer.twinlink.trigger.TwinlinkTrigger;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Table(name = "twinlink_trigger")
public class TwinlinkTriggerEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twinlink_id")
    private UUID twinlinkId;

    @Column(name = "order")
    private Integer order;

    @Column(name = "twinlink_trigger_featurer_id")
    private Integer twinlinkTriggerFeaturerId;

    @FeaturerList(type = TwinlinkTrigger.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "twinlink_trigger_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity twinlinkTriggerFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "twinlink_trigger_params", columnDefinition = "hstore")
    private HashMap<String, String> twinlinkTriggerParams;
}
