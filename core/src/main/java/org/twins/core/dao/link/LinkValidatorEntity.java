package org.twins.core.dao.link;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.twins.core.dao.domain.DomainVersionEntity;
import org.twins.core.domain.versioning.DomainSetting;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@DomainSetting
@Table(name = "link_validator")
public class LinkValidatorEntity {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "link_id")
    private UUID twinflowTransitionId;

    @Column(name = "domain_version_id")
    private UUID domainVersionId;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_version_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainVersionEntity domainVersion;
}
