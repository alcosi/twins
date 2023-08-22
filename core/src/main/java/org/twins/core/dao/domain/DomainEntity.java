package org.twins.core.dao.domain;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.cambium.featurer.annotations.FeaturerList;

import jakarta.persistence.*;
import org.cambium.featurer.dao.FeaturerEntity;
import org.twins.core.featurer.businessaccount.initiator.BusinessAccountInitiator;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Table(name = "domain")
@DynamicUpdate
@Data
public class DomainEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "key")
    private String key;

    @Column(name = "description")
    private String description;

    @FeaturerList(type = BusinessAccountInitiator.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "business_account_initiator_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity businessAccountInitiatorFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "business_account_initiator_params", columnDefinition = "hstore")
    private HashMap<String, String> businessAccountInitiatorParams;
}
