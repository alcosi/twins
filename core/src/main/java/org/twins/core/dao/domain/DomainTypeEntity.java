package org.twins.core.dao.domain;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.cambium.featurer.annotations.FeaturerList;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.twins.core.featurer.domain.initiator.DomainInitiator;
import org.twins.core.featurer.tokenhandler.TokenHandler;
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

    @FeaturerList(type = DomainInitiator.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "domain_initiator_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity domainInitiatorFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "domain_initiator_params", columnDefinition = "hstore")
    private HashMap<String, String> domainInitiatorParams;

    @FeaturerList(type = TokenHandler.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "default_token_handler_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity defaultTokenHandlerFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "default_token_handler_params", columnDefinition = "hstore")
    private HashMap<String, String> defaultTokenHandlerParams;

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
