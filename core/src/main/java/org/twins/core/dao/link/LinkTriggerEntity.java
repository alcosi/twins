package org.twins.core.dao.link;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Table(name = "link_trigger")
public class LinkTriggerEntity {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "link_id")
    private UUID linkId;

    @Column(name = "`order`")
    @Basic
    private Integer order;

    @Column(name = "link_trigger_featurer_id")
    private Integer linkTriggerFeaturerId;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FeaturerEntity linkTriggerFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "link_trigger_params", columnDefinition = "hstore")
    private HashMap<String, String> linkTriggerParams;
}
