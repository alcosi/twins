package org.twins.core.dao.link;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import org.cambium.featurer.annotations.FeaturerList;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.twins.core.featurer.link.trigger.LinkTrigger;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Table(name = "link_trigger")
public class LinkTriggerEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "link_id")
    private UUID linkId;

    @Column(name = "order")
    private Integer order;

    @Column(name = "link_trigger_featurer_id")
    private Integer linkTriggerFeaturerId;

    @FeaturerList(type = LinkTrigger.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "link_trigger_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity linkTriggerFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "link_trigger_params", columnDefinition = "hstore")
    private HashMap<String, String> linkTriggerParams;
}
