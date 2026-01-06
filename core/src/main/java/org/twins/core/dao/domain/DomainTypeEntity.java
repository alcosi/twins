package org.twins.core.dao.domain;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.cambium.featurer.annotations.FeaturerList;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.twins.core.dao.idp.IdentityProviderEntity;
import org.twins.core.featurer.domain.initiator.DomainInitiator;
import org.twins.core.featurer.usergroup.manager.UserGroupManager;

import java.util.HashMap;

@Entity
@Table(name = "domain_type")
@DynamicUpdate
@Data
@Accessors(chain = true)
public class DomainTypeEntity implements EasyLoggable {
    @Id
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @FeaturerList(type = DomainInitiator.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "domain_initiator_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity domainInitiatorFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "domain_initiator_params", columnDefinition = "hstore")
    private HashMap<String, String> domainInitiatorParams;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "default_identity_provider_id", insertable = false, updatable = false)
    private IdentityProviderEntity defaultIdentityProvider;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @FeaturerList(type = UserGroupManager.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "default_user_group_manager_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity defaultUserGroupManagerFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "default_user_group_manager_params", columnDefinition = "hstore")
    private HashMap<String, String> defaultUserGroupManagerParams;

    public String easyLog(Level level) {
        return "domainType[id:" + id + "]";
    }
}
