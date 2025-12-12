package org.twins.core.dao.link;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Table(name = "link_validator")
public class LinkValidatorEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "link_id")
    private UUID twinflowTransitionId;

    @Column(name = "`order`")
    @Basic
    private Integer order;

    @Column(name = "link_validator_featurer_id")
    private Integer linkValidatorFeaturerId;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FeaturerEntity linkValidatorFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "link_validator_params", columnDefinition = "hstore")
    private HashMap<String, String> linkValidatorParams;
}
